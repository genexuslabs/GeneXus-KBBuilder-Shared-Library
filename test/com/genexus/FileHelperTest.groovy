import static org.junit.Assert.*
import org.junit.Test

class FileHelperTest {
    @Test
    void testStandardizeVersionForSemVer() {
        FileHelper fileHelper = new FileHelper()

        // Test case 1: Standard version without label
        String result1 = fileHelper.standarizeVersionForSemVer("1.3.5", "88", "")
        assertEquals("1.3.88", result1)

        // Test case 2: Version with 'beta' label
        String result2 = fileHelper.standarizeVersionForSemVer("2.1.5", "457", "beta")
        assertEquals("2.1.0-beta.457", result2)

        // Test case 3: Increase major version by 100 without label
        String result3 = fileHelper.standarizeVersionForSemVer("1.3.5", "88", "", 100)
        assertEquals("101.3.88", result3)
    }
}
