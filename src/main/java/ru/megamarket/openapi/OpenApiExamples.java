package ru.megamarket.openapi;

public class OpenApiExamples {

    public final static String VALIDATION_FAILED_ERROR = "{\"code\": 400, \"message\":\"Validation failed\"}";
    public final static String ITEM_NOT_FOUND_ERROR = "{\"code\": 404, \"message\":\"Item not found\"}";
    public final static String ID = "3fa85f64-5717-4562-b3fc-2c963f66a333";
    public final static String DATE = "3fa85f64-5717-4562-b3fc-2c963f66a333";
    public final static String UNIT = "{ \"id\": \"3fa85f64-5717-4562-b3fc-2c963f66a111\"," +
            " \"name\": \"Категория\"," +
            " \"type\": \"CATEGORY\"," +
            " \"parentId\": null," +
            " \"date\": \"2022-05-28T21:12:01.516Z\"," +
            " \"price\": 6," +
            " \"children\": [{ \"name\": \"Оффер 1\"," +
            " \"id\": \"3fa85f64-5717-4562-b3fc-2c963f66a222\"," +
            " \"price\": 4," +
            " \"date\": \"2022-05-28T21:12:01.516Z\"," +
            " \"type\": \"OFFER\"," +
            " \"parentId\": \"3fa85f64-5717-4562-b3fc-2c963f66a111\" }," +
            " { \"name\": \"Подкатегория\"," +
            " \"type\": \"CATEGORY\"," +
            " \"id\": \"3fa85f64-5717-4562-b3fc-2c963f66a333\"," +
            " \"date\": \"2022-05-26T21:12:01.516Z\"," +
            " \"parentId\": \"3fa85f64-5717-4562-b3fc-2c963f66a111\"," +
            " \"price\": 8," +
            " \"children\": [{ \"name\": \"Оффер 2\"," +
            " \"id\": \"3fa85f64-5717-4562-b3fc-2c963f66a444\"," +
            " \"parentId\": \"3fa85f64-5717-4562-b3fc-2c963f66a333\"," +
            " \"date\": \"2022-05-26T21:12:01.516Z\"," +
            " \"price\": 8, \"type\": \"OFFER\" }]}]}";
    public final static String IMPORT = "{ \"id\": \"3fa85f64-5717-4562-b3fc-2c963f66a444\"," +
            " \"name\": \"Оффер\"," +
            " \"parentId\": \"3fa85f64-5717-4562-b3fc-2c963f66a333\"," +
            " \"price\": 234," +
            " \"type\": \"OFFER\" }";
    public final static String STATISTIC = "{ \"id\": \"3fa85f64-5717-4562-b3fc-2c963f66a444\"," +
            " \"name\": \"Оффер\"," +
            " \"date\": \"2022-05-28T21:12:01.000Z\"," +
            " \"parentId\": \"3fa85f64-5717-4562-b3fc-2c963f66a333\"," +
            " \"price\": 234," +
            " \"type\": \"OFFER\" }";

}
