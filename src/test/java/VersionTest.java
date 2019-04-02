import io.manebot.Version;
import junit.framework.TestCase;

public class VersionTest extends TestCase {

    public static void main(String[] args) throws Exception {
        new VersionTest().testParser();
    }

    public void testParser() throws Exception {
        assertEquals("1.0.0-abcdefg", Version.fromString("1.0.0-abcdefg").toString());
        assertEquals("1.0-abcdefg", Version.fromString("1.0-abcdefg").toString());
        assertEquals("1-abcdefg", Version.fromString("1-abcdefg").toString());
        assertEquals("1.0.0", Version.fromString("1.0.0").toString());
        assertEquals("1.0", Version.fromString("1.0").toString());
        assertEquals("1", Version.fromString("1").toString());

        assertEquals(Version.fromString("1.0.0.0.0").compareTo(Version.fromString("1.0.0.0")), 0);
        assertEquals(Version.fromString("1.0.0.0").compareTo(Version.fromString("1.0.0.0.0")), 0);
    }

}
