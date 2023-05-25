import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComponentsTest{
    private static String baseUrl = "https://api.instatus.com/v1/";
    private static String components = "/components";
    private static String testUrl = baseUrl;
    private static String ValidComponentUrl;
    private static String bearerToken;
    private static String pageId;
    private static int maxTryCount = 1;

    public void SetToken(String token) {
        bearerToken = token;
    }

    @BeforeClass
    @Parameters("bearerToken")
    public void SetValidPageId(String token) {
        SetToken(token);
        String preStepUrl = baseUrl + "pages";

        for (int PageIndex = 1; PageIndex <= maxTryCount; PageIndex++)
        {
            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + bearerToken)
                    .when()
                    .get(preStepUrl + "?page=" + PageIndex)
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            if(response.getStatusCode() != 200)
                throw new SkipException("Response not valid");

            List<String> idList = response.jsonPath().getList("id");

            if(idList.size() > 0)
            {
                pageId = idList.get(0);
                ValidComponentUrl = testUrl + pageId + components;
                return;
            }
        }

        throw new SkipException("Invalid page id not found. Skipping test suite.");
    }

    public String GetComponentId() {

        for (int PageIndex = 1; PageIndex <= maxTryCount; PageIndex++)
        {
            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + bearerToken)
                    .when()
                    .get(ValidComponentUrl)
                    .then()
                    .extract()
                    .response();

            if(response.getStatusCode() != 200)
                throw new SkipException("Response not valid");

            List<String> idList = response.jsonPath().getList("id");

            if(idList.size() > 0)
            {
                return idList.get(0);
            }
        }

        throw new SkipException("Cannot obtain component id. Skipping test.");
    }

    @Test
    public void GetComponentsValidId_ExpectOK() {
        Response responsePage1 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(testUrl + pageId + components)
                .then()
                .extract()
                .response();

        Assert.assertEquals(responsePage1.getStatusCode(), 200);
    }

    @Test
    public void PagesAndPage1Param_Eq() {
        String withoutPageParam = ValidComponentUrl;
        String withPageParam = withoutPageParam + "?page=1";
        Response responsePage = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(withoutPageParam)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Response responsePage1 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(withPageParam)
                .then()
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
                .get(ValidComponentUrl)
                .then()
                .statusCode(200)
                .extract()
                .response();

        ArrayList<Object> list = response.body().path("");
        int listSize = list.size();

        if(listSize < 1)
            throw new SkipException("List size not enough to conduct TC");

        int expectedListSize = listSize - 1;
        String params = "?page=1&per_page=" + expectedListSize;

        response = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(ValidComponentUrl + params)
                .then()
                .statusCode(200)
                .extract()
                .response();

        list = response.body().path("");
        Assert.assertEquals(list.size(), expectedListSize);
    }

    @Test
    public void ParamPerPageAboveMax_ExpectMax() {
        int maxPerPageElem = 100;
        String maxPerPageExceeded= ValidComponentUrl + "?page=1&per_page=" + maxPerPageElem + 1;
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(maxPerPageExceeded)
                .then()
                .statusCode(200)
                .extract()
                .response();

        ArrayList<Object> list = response.body().path("");
        Assert.assertTrue(list.size() <= maxPerPageElem);
    }

    @Test
    public void DifferentPages_NotEq() {
        String pageUrl = ValidComponentUrl + "?page=";
        Response responsePage1 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(pageUrl + 1)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Response responsePage2 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(pageUrl + 2)
                .then()
                .statusCode(200)
                .extract()
                .response();

        ArrayList<Object> page1Body = responsePage1.body().path("");
        ArrayList<Object> page2Body = responsePage2.body().path("");
        Assert.assertFalse(page1Body.equals(page2Body));
    }

    @Test
    public void InvalidParamPageId_Expect422() {
        String pageUrl = ValidComponentUrl + "?page=INVALID";
        Response responsePage1 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(pageUrl)
                .then()
                .extract()
                .response();

        Assert.assertEquals(responsePage1.getStatusCode(), 422);
    }

    @Test
    public void InvalidParamPerPage_Expect422() {
        String pageUrl = ValidComponentUrl + "?page=1&per_page=INVALID";
        Response responsePage1 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(pageUrl)
                .then()
                .extract()
                .response();

        Assert.assertEquals(responsePage1.getStatusCode(), 422);
    }

    @Test
    public void OptionalParamPage_Expect200() {
        String withPageUrl = ValidComponentUrl + "?page=1&per_page=1";
        String withoutPageUrl = ValidComponentUrl + "?per_page=1";

        Response responseWithPage = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(withPageUrl)
                .then()
                .extract()
                .response();

        Response responseWithoutPage = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(withoutPageUrl)
                .then()
                .extract()
                .response();

        ArrayList<Object> withPageBody = responseWithPage.body().path("");
        ArrayList<Object> withoutPageBody = responseWithoutPage.body().path("");
        Assert.assertTrue(withPageBody.equals(withoutPageBody));
    }

    @Test
    public void ValidStatus_200() {
        List<String> expectedStatusList = Arrays.asList(
                "OPERATIONAL",
                "UNDERMAINTENANCE",
                "DEGRADEDPERFORMANCE",
                "PARTIALOUTAGE",
                "MAJOROUTAGE"
        );

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(ValidComponentUrl)
                .then()
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

    @Test
    public void GetIncidentValidId_ExpectOK() {
        String componentId = GetComponentId();
        Response responsePage1 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(ValidComponentUrl + "/" + componentId)
                .then()
                .contentType(ContentType.JSON)
                .extract()
                .response();

        Assert.assertEquals(responsePage1.getStatusCode(), 200);
    }

    @Test
    public void GetIncidentUppercaseId_Expect500() {
        String componentId = GetComponentId();
        Response responsePage1 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(ValidComponentUrl + "/" + componentId.toUpperCase())
                .then()
                .contentType(ContentType.JSON)
                .extract()
                .response();

        Assert.assertEquals(responsePage1.getStatusCode(), 500);
    }

    @Test
    public void GetIncidentInvalidId_Expect500() {
        String componentId = "cl2pyu4b049026i4n45qfoowx0";
        Response responsePage1 = RestAssured.given()
                .header("Authorization", "Bearer " + bearerToken)
                .when()
                .get(ValidComponentUrl + "/" + componentId)
                .then()
                .contentType(ContentType.JSON)
                .extract()
                .response();

        Assert.assertEquals(responsePage1.getStatusCode(), 500);
    }

}


