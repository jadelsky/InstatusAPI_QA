import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ValidationTest {
    public static String baseUrl = "https://api.instatus.com/v1/";
    private String testUrl = baseUrl + "pages/";

    @Test
    public void withoutToken_401() {
        Response response = RestAssured.given()
                .when()
                .get(testUrl)
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 401);
    }

    @Test
    public void wrongToken_401() {
        String wrongBarerToken = "8d16404936e72e705980878e18c95976";
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + wrongBarerToken)
                .when()
                .get(testUrl)
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 401);
    }

    @Test
    @Parameters("bearerToken")
    public void UppercaseToken_401(String bearerToken) {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken.toUpperCase())
                .when()
                .get(testUrl)
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 401);
    }

    @Test
    @Parameters("bearerToken")
    public void validToken_200(String bearerToken) {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(testUrl)
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertTrue(response.getContentType().contains("application/json"));
    }

    @Test
    @Parameters("bearerToken")
    public void wrongContentType_ExpectJson(String bearerToken) {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .header("Content-Type", "application/ecmascript")
                .when()
                .get(testUrl)
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertTrue(response.getContentType().contains("application/json"));
    }

}
