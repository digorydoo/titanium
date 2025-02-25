package ch.digorydoo.titanium.engine.shader

import ch.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags.CONTOUR
import ch.digorydoo.titanium.engine.shader.ShaderManager.ShaderFlags.SHADOWS
import ch.digorydoo.titanium.engine.shader.ShaderPrecompiler.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ShaderPrecompilerTest {
    @Test
    fun `should return an empty string if given an empty string`() {
        val precompiler = ShaderPrecompiler()
        assertTrue(precompiler.precompile("", setOf()) == "")
        assertTrue(precompiler.precompile("", setOf(SHADOWS)) == "")
    }

    @Test
    fun `should trim start and end of lines`() {
        val input = """           one
                two
            three        """
        val expected = "one\ntwo\nthree"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should remove line comments`() {
        val input = """
            test {      // one two three
                doIt(); // now
            }           // yeah
        """
        val expected = "test {\ndoIt();\n}"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should not mistake a single slash for a line comment`() {
        val input = """
            float x = 4.0f / 2.0f
            float y = 1/2 + 3/4
        """
        val expected = "float x = 4.0f / 2.0f\nfloat y = 1/2 + 3/4"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should remove block comments on same line`() {
        val input = """
            int /* comment */ x = 42;
            int /**/ y = 43;
            int/**/z = 44;
            /* entire line */
            int a = 1; /* end of line */
        """
        val expected = "int   x = 42;\nint   y = 43;\nint z = 44;\nint a = 1;"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should remove block comments spanning multiple lines`() {
        val input = """
            int/*x = 42;
            int y = 43;
            int*/z = 44; 
        """
        val expected = "int\nz = 44;"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should throw if block comment is not closed`() {
        val input = """
            int /* x = 42;
            int y = 43;
        """
        assertThrows<BlockCommentNotClosedError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should handle combined block and line comments in the expected way`() {
        var input = """
            //* should be line comment
            no comment
            /*/ should be block comment
            still in block comment
            //*/ out again
        """
        var expected = "no comment\nout again"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))

        input = """
            /* should be block comment
            still in block comment
            /*/ should be out again
            no comment
            //*/ should be in line comment
        """
        expected = "should be out again\nno comment"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should keep defines`() {
        val input = """
            before
            #define ONE 1
            #define TWO 2
            #define THREE
            after
        """
        val expected = "before\n#define ONE 1\n#define TWO 2\n#define THREE\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should throw if a define contains an invalid character`() {
        val input = """
            before
            #define YOUCÄNNOT
            after
        """
        assertThrows<BadNameForDefineError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should not throw if a define is used as a macro`() {
        val input = """
            before
            #define MACRO(x) (x + 1)
            after
        """
        val expected = "before\n#define MACRO(x) (x + 1)\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should keep version`() {
        val input = """
            before
            #version 410 core
            after
        """
        val expected = "before\n#version 410 core\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should throw if a define is missing its name`() {
        val input = """
            before
            #define
            after
        """
        assertThrows<MissingKeyInDefineError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should throw if a define follows another without undef in between`() {
        val input = """
            before
            #define SOMETHING 1
            between
            #define SOMETHING 2
            after
        """
        assertThrows<DuplicateDefineError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should not throw if a define follows another when there is an undef in between`() {
        val input = """
            before
            #define SOMETHING
            #undef SOMETHING
            #define SOMETHING
            after
        """
        val expected = "before\n#define SOMETHING\n#undef SOMETHING\n#define SOMETHING\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should throw if an undef is missing its key`() {
        val input = """
            before
            #undef // what
            after
        """
        assertThrows<MissingKeyInUndefError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should throw if an undef has more than one argument`() {
        val input = """
            before
            #undef THIS THAT
            after
        """
        assertThrows<TooManyArgumentsError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should throw if a define is duplicated by a flag we set from outside`() {
        val input = """
            before
            #define SHADOWS
            after
        """
        assertThrows<DuplicateDefineError> { ShaderPrecompiler().precompile(input, setOf(SHADOWS)) }
    }

    @Test
    fun `should not throw if a define is duplicated by a flag when an undef precedes it`() {
        val input = """
            before
            #undef SHADOWS
            #define SHADOWS
            after
        """
        val expected = "before\n#undef SHADOWS\n#define SHADOWS\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should throw if an endif is seen without a matching if or ifdef`() {
        val input = """
            before
            #endif
            after
        """
        assertThrows<EndifWithoutIfError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should throw if there is no endif for an ifdef`() {
        val input = """
            before
            #ifdef SHADOWS
            shadow
        """
        assertThrows<MissingEndifError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should throw if an ifdef is missing its key`() {
        val input = """
            before
            #ifdef // missing
            what
            #endif
            after
        """
        assertThrows<MissingKeyInIfdefError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should throw if an ifdef has more than one argument`() {
        val input = """
            before
            #ifdef THIS CANNOTBE
            what
            #endif
            after
        """
        assertThrows<TooManyArgumentsError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should throw if an ifndef is missing its key`() {
        val input = """
            before
            #ifndef // missing
            what
            #endif
            after
        """
        assertThrows<MissingKeyInIfndefError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should throw if an ifndef has more than one argument`() {
        val input = """
            before
            #ifndef THIS CANNOTBE
            what
            #endif
            after
        """
        assertThrows<TooManyArgumentsError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should remove the ifdef block if the condition is not met`() {
        val input = """
            before define
            #define SOMETHING
            before ifdef
            #ifdef SOMETHING_ELSE
            inside
            #endif
            after
        """
        val expected = "before define\n#define SOMETHING\nbefore ifdef\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should remove the ifndef block if the condition is met`() {
        val input = """
            before define
            #define SOMETHING
            before ifndef
            #ifndef SOMETHING
            inside
            #endif
            after
        """
        val expected = "before define\n#define SOMETHING\nbefore ifndef\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should keep the ifdef block if the condition is met through an internal define`() {
        val input = """
            before define
            #define SOMETHING
            before ifdef
            #ifdef SOMETHING
            inside
            #endif
            after
        """
        val expected = "before define\n#define SOMETHING\nbefore ifdef\ninside\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should keep the ifdef block if the condition is met through an external flag`() {
        val input = """
            before
            #ifdef SHADOWS
            inside
            #endif
            after
        """
        val expected = "before\ninside\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS)))
    }

    @Test
    fun `should drop the ifndef block if the condition is met through an external flag`() {
        val input = """
            before
            #ifndef SHADOWS
            inside
            #endif
            after
        """
        val expected = "before\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS)))
    }

    @Test
    fun `should keep both blocks if a nested ifdef and its parent both met their condition`() {
        val input = """
            before define
            #define SOMETHING
            before outer
            #ifdef SHADOWS
                before inner
                #ifdef SOMETHING
                inside
                #endif
                after inner
            #endif
            out again
        """
        val expected = "before define\n#define SOMETHING\nbefore outer\nbefore inner\ninside\nafter inner\nout again"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS)))
    }

    @Test
    fun `should remove the inner block if a nested ifdef does not meet its condition while its parent does`() {
        val input = """
            before outer
            #ifdef SHADOWS
                before inner
                #ifdef SOMETHING
                inside
                #endif
                after inner
            #endif
            out again
        """
        val expected = "before outer\nbefore inner\nafter inner\nout again"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS)))
    }

    @Test
    fun `should remove both blocks if the parent ifdef does not meet its condition even if the nested one does`() {
        val input = """
            before define
            #define SOMETHING
            before outer
            #ifdef SHADOWS
                before inner
                #ifdef SOMETHING
                inside
                #endif
                after inner
            #endif
            out again
        """
        val expected = "before define\n#define SOMETHING\nbefore outer\nout again"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should throw if the else block has an argument`() {
        val input = """
            before
            #ifdef SHADOWS
            shadow
            #else OTHERWISE
            otherwise
            #endif
            after
        """
        assertThrows<DirectiveCannotHaveArgsError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should throw if an else block is seen without any matching if or ifdef`() {
        val input = """
            before
            #else
            what
            #endif
            after
        """
        assertThrows<ElseWithoutIfError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should not mistake a comment for an argument`() {
        val input = """
            before
            #ifdef SHADOWS
            shadow
            #else // OTHERWISE
            otherwise
            #endif
            after
        """
        val expected = "before\notherwise\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should remove the else block when an ifdef's condition is met`() {
        val input = """
            before
            #ifdef SHADOWS
            has shadow
            #else
            has no shadow
            #endif
            after
        """
        val expected = "before\nhas shadow\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS)))
    }

    @Test
    fun `should remove the else block when an ifndef's condition is met`() {
        val input = """
            before
            #ifndef SHADOWS
            no shadow
            #else
            has shadow
            #endif
            after
        """
        val expected = "before\nno shadow\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should keep the else block when an ifdef's condition is not met`() {
        val input = """
            before
            #ifdef SHADOWS
            has shadow
            #else
            has no shadow
            #endif
            after
        """
        val expected = "before\nhas no shadow\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should throw if an elifdef is seen that does not refer to any if or ifdef`() {
        val input = """
            before
            #elifdef BLAH
            blah
            #endif
            after
        """
        assertThrows<ElifdefWithoutIfError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should throw if an elifdef has more than one argument`() {
        val input = """
            before
            #elifdef THIS CANNOTBE
            what
            #endif
            after
        """
        assertThrows<TooManyArgumentsError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should keep the if block and drop the elif and else blocks when both conditions are met`() {
        val input = """
            #define SOMETHING
            #ifdef SOMETHING
            something
            #elifdef SHADOWS
            shadow
            #else
            nope
            #endif
            after
        """
        val expected = "#define SOMETHING\nsomething\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS)))
    }

    @Test
    fun `should keep the elif block and drop the if and else blocks when the 1st condition is not met, but the 2nd`() {
        val input = """
            before
            #ifdef SOMETHING
            something
            #elifdef SHADOWS
            shadow
            #else
            nope
            #endif
            after
        """
        val expected = "before\nshadow\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS)))
    }

    @Test
    fun `should drop if and elif blocks and keep the else block when neither condition is met`() {
        val input = """
            before
            #ifdef SOMETHING
            something
            #elifdef SHADOWS
            shadow
            #else
            nope
            #endif
            after
        """
        val expected = "before\nnope\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should be able to use if and ifdef when the define is a macro`() {
        val input = """
            before
            #define TEST(x, y) ((x) * (y))
            #ifdef TEST
            ok1
            #endif
            #if defined(TEST)
            ok2
            #endif
            after
        """
        val expected = "before\n#define TEST(x, y) ((x) * (y))\nok1\nok2\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should throw if an ifdef is used together with AND`() {
        val input = """
            before
            #ifdef BLAH&&BLUPP
            nope
            #endif
            after
        """
        assertThrows<BadNameForDefineError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should throw if an ifdef is used together with OR`() {
        val input = """
            before
            #ifdef BLAH||BLUPP
            nope
            #endif
            after
        """
        assertThrows<BadNameForDefineError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should throw if an unknown directive is used`() {
        val input = """
            before
            #whatever BLAH
            after
        """
        assertThrows<DirectiveNotImplementedError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should be able to evaluate if 0`() {
        val input = """
            before
            #if 0
            not this
            #else
            that
            #endif
            after
        """
        val expected = "before\nthat\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should be able to evaluate if 1`() {
        val input = """
            before
            #if 1
            this
            #else
            not that
            #endif
            after
        """
        val expected = "before\nthis\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should be able to evaluate if 2`() {
        val input = """
            before
            #if 2
            this
            #else
            not that
            #endif
            after
        """
        val expected = "before\nthis\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should be able to evaluate AND conditions inside if`() {
        val input = """
            before
            #if 1 && 1
               ok1
            #endif
            #if 1 && 0
               bad1
            #endif
            #if 0 && 1
               bad2
            #endif
            #if 0 && 0
               bad3
            #endif
            after
        """
        val expected = "before\nok1\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should be able to evaluate OR conditions inside if`() {
        val input = """
            before
            #if 1 || 1
               ok1
            #endif
            #if 1 || 0
               ok2
            #endif
            #if 0 || 1
               ok3
            #endif
            #if 0 || 0
               bad1
            #endif
            after
        """
        val expected = "before\nok1\nok2\nok3\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf()))
    }

    @Test
    fun `should be able to evaluate defined() expressions`() {
        val input = """
            before
            #if defined(SHADOWS)
               ok1
            #endif
            #if defined( SHADOWS )
               ok2
            #endif
            #if defined(BLAH)
               bad1
            #endif
            after
        """
        val expected = "before\nok1\nok2\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS)))
    }

    @Test
    fun `should be able to evaluate NOT defined() expressions`() {
        val input = """
            before
            #if !defined(SHADOWS)
               bad1
            #endif
            #if !defined( SHADOWS )
               bad2
            #endif
            #if !defined(BLAH)
               ok1
            #endif
            after
        """
        val expected = "before\nok1\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS)))
    }

    @Test
    fun `should be able to combine defined() with AND conditions`() {
        val input = """
            before
            #if defined(SHADOWS) && defined(CONTOUR)
               ok1
            #endif
            #if defined( SHADOWS )&&defined( CONTOUR )
               ok2
            #endif
            #if defined(SHADOWS) && defined(BLAH)
               bad1
            #endif
            #if defined(BLAH) && defined(SHADOWS)
               bad2
            #endif
            #if defined(BLAH) && defined(BLUPP)
               bad3
            #endif
            after
        """
        val expected = "before\nok1\nok2\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS, CONTOUR)))
    }

    @Test
    fun `should be able to combine NOT defined() with AND conditions`() {
        val input = """
            before
            #if !defined(SHADOWS) && !defined(CONTOUR)
               bad1
            #endif
            #if !defined( SHADOWS )&&!defined( CONTOUR )
               bad2
            #endif
            #if !defined(SHADOWS) && !defined(BLAH)
               bad3
            #endif
            #if !defined(BLAH) && !defined(SHADOWS)
               bad4
            #endif
            #if !defined(BLAH) && !defined(BLUPP)
               ok1
            #endif
            after
        """
        val expected = "before\nok1\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS, CONTOUR)))
    }

    @Test
    fun `should be able to combine defined() with OR conditions`() {
        val input = """
            before
            #if defined(SHADOWS) || defined(CONTOUR)
               ok1
            #endif
            #if defined( SHADOWS )||defined( CONTOUR )
               ok2
            #endif
            #if defined(SHADOWS) || defined(BLAH)
               ok3
            #endif
            #if defined(BLAH) || defined(SHADOWS)
               ok4
            #endif
            #if defined(BLAH) || defined(BLUPP)
               bad1
            #endif
            after
        """
        val expected = "before\nok1\nok2\nok3\nok4\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS, CONTOUR)))
    }

    @Test
    fun `should be able to combine NOT defined() with OR conditions`() {
        val input = """
            before
            #if !defined(SHADOWS) || !defined(CONTOUR)
               bad1
            #endif
            #if !defined( SHADOWS )||!defined( CONTOUR )
               bad2
            #endif
            #if !defined(SHADOWS) || !defined(BLAH)
               ok1
            #endif
            #if !defined(BLAH) || !defined(SHADOWS)
               ok2
            #endif
            #if !defined(BLAH) || !defined(BLUPP)
               ok3
            #endif
            after
        """
        val expected = "before\nok1\nok2\nok3\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS, CONTOUR)))
    }

    @Test
    fun `should evaluate AND conditions with higher precedence than OR conditions`() {
        val input = """
            before
            #if defined(BLAH) && defined(BLUPP) || defined(CONTOUR)
            ok1
            #endif
            #if defined(SHADOWS) || defined(BLUPP) && defined(CONTOUR)
            ok2
            #endif
            #if defined(SHADOWS) && defined(BLAH) || defined(CONTOUR) || defined(BLUPP) && defined(YIKES)
            ok3
            #endif
            after
        """
        val expected = "before\nok1\nok2\nok3\nafter"
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS, CONTOUR)))
    }

    @Test
    fun `should throw if parentheses are used since they are not implemented`() {
        val input = """
            before
            #if (defined(BLAH) && defined(BLUPP)) || defined(CONTOUR)
            nope
            #endif
            after
        """
        assertThrows<InvalidCondExprError> { ShaderPrecompiler().precompile(input, setOf(SHADOWS, CONTOUR)) }
    }

    @Test
    fun `should throw if an if expression is used like an ifdef`() {
        val input = """
            before
            #if NOTSUPPORTED
            this
            #else
            that
            #endif
            after
        """
        assertThrows<InvalidCondExprError> { ShaderPrecompiler().precompile(input, setOf()) }
    }

    @Test
    fun `should handle a complex example in the expected way`() {
        val input = """
            /*
             * Example /* this is not a nested comment
             */
            #version 410 core
            #define HAZINESS_MAX 0.9f // maximum amount of applying HazyColour
            
            #ifndef BLAH
                uniform float Blah;
            #else
                #error why is BLAH defined
            #endif
            
            #if defined(BLAH) || defined(BLUPP)
                #error should not happen
            #else
                #ifdef SHADOWS // passed in from outside
                    uniform float ShadowIntensity;
                // #else
                    uniform float AhIForgot;
                /*
                #else
                */
                    uniform float ForgotThisToo;
                #else
                    #error // should never come here
                #endif
            #endif
            
            #ifdef HAZINESS_MAX
                uniform vec3 HazyColour;

                #ifdef HAZY_MORITZ
                    #error // should never come here
                #elifdef SHADOWS
                    uniform float HazyShadow;
                #else
                    #error // should never come here
                #endif
            /*
            #endif
            */
            #else
                #error // should never come here
            #endif // now we close it
            uniform float Bye;
        """
        val expected = """
            #version 410 core
            #define HAZINESS_MAX 0.9f
            uniform float Blah;
            uniform float ShadowIntensity;
            uniform float AhIForgot;
            uniform float ForgotThisToo;
            uniform vec3 HazyColour;
            uniform float HazyShadow;
            uniform float Bye;
        """.trimIndent()
        assertEquals(expected, ShaderPrecompiler().precompile(input, setOf(SHADOWS)))
    }
}
