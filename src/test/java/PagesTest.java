import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PagesTest{
    private static String baseUrl = "https://api.instatus.com/v1/";
    private static String testUrl = baseUrl + "pages";
    private static String bearerToken;

    @BeforeTest
    @Parameters("bearerToken")
    public void SetToken(String token) {
        bearerToken = token;
    }

    @Test
    public void PagesAndPage1Param_Eq() {
        Response responsePage = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(testUrl)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .extract()
                .response();

        String page1Url = "?page=1";

        Response responsePage1 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(testUrl + page1Url)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .extract()
                .response();

        ArrayList<Object> pageBody = responsePage.body().path("");
        ArrayList<Object> page1Body = responsePage1.body().path("");
        Assert.assertTrue(pageBody.equals(page1Body));
    }

    @Test
    public void ParamPerPage_AsProvided() {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(testUrl)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .extract()
                .response();

        ArrayList<Object> list = response.body().path("");
        int listSize = list.size();

        if(listSize < 1)
            throw new SkipException("List size not enough to conduct TC");

        int expectedListSize = listSize - 1;
        String PerPageUrl = testUrl + "?page=1&per_page=" + expectedListSize;

        response = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(PerPageUrl)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .extract()
                .response();

        list = response.body().path("");
        Assert.assertEquals(list.size(), expectedListSize);
    }

    @Test
    public void ParamPerPageAboveMax_ExpectMax() {
        int maxPerPageElem = 100;
        String maxPerPageExceeded= "?page=1&per_page=" + maxPerPageElem + 1;
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(maxPerPageExceeded)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .extract()
                .response();

        ArrayList<Object> list = response.body().path("");

        Assert.assertTrue(list.size() <= maxPerPageElem);
    }

    @Test
    public void DifferentPages_NotEq() {
        String pageUrl =testUrl + "?page=";
        Response responsePage1 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(pageUrl + 1)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .extract()
                .response();

        Response responsePage2 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(pageUrl + 2)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .extract()
                .response();

        ArrayList<Object> page1Body = responsePage1.body().path("");
        ArrayList<Object> page2Body = responsePage2.body().path("");
        Assert.assertFalse(page1Body.equals(page2Body));
    }

    @Test
    public void InvalidParamPage_Expect422() {
        String pageUrl =testUrl + "?page=INVALID";
        Response responsePage1 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(pageUrl)
                .then()
                .contentType(ContentType.JSON)
                .extract()
                .response();

        Assert.assertEquals(responsePage1.getStatusCode(), 422);
    }

    @Test
    public void InvalidParamPerPage_Expect422() {
        String pageUrl =testUrl + "?page=1&per_page=INVALID";
        Response responsePage1 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(pageUrl)
                .then()
                .contentType(ContentType.JSON)
                .extract()
                .response();

        Assert.assertEquals(responsePage1.getStatusCode(), 422);
    }

    @Test
    public void OptionalParamPage_Expect200() {
        String withPageUrl =testUrl + "?page=1&per_page=1";
        String withoutPageUrl =testUrl + "?per_page=1";

        Response responseWithPage = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(withPageUrl)
                .then()
                .contentType(ContentType.JSON)
                .extract()
                .response();

        Response responseWithoutPage = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(withoutPageUrl)
                .then()
                .contentType(ContentType.JSON)
                .extract()
                .response();

        ArrayList<Object> withPageBody = responseWithPage.body().path("");
        ArrayList<Object> withoutPageBody = responseWithoutPage.body().path("");
        Assert.assertTrue(withPageBody.equals(withoutPageBody));
    }

    @Test
    public void ValidStatus_200() {

        List<String> expectedStatusList = Arrays.asList(
                "UP",
                "HASISSUES",
                "ALLUNDERMAINTENANCE",
                "ALLDEGRADEDPERFORMANCE",
                "ALLPARTIALOUTAGE",
                "ALLMINOROUTAGE",
                "ALLMAJOROUTAGE",
                "SOMEUNDERMAINTENANCE",
                "SOMEDEGRADEDPERFORMANCE",
                "SOMEPARTIALOUTAGE",
                "SOMEMINOROUTAGE",
                "SOMEMAJOROUTAGE",
                "ONEUNDERMAINTENANCE",
                "ONEDEGRADEDPERFORMANCE",
                "ONEPARTIALOUTAGE",
                "ONEMINOROUTAGE",
                "ONEMAJOROUTAGE"
        );

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(testUrl)
                .then()
                .contentType(ContentType.JSON)
                .extract()
                .response();

        List<String> statusList = response.jsonPath().getList("status");
        boolean allStatusValid = true;

        for (String status : statusList) {
            if (!expectedStatusList.contains(status)) {
                allStatusValid = false;
            }
        }

        Assert.assertTrue(allStatusValid);
    }

}
