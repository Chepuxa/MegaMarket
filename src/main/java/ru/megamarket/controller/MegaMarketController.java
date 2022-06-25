package ru.megamarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.megamarket.dto.Error;
import ru.megamarket.dto.ShopUnit;
import ru.megamarket.dto.ShopUnitImportRequest;
import ru.megamarket.dto.ShopUnitStatisticResponse;
import ru.megamarket.exceptions.CustomValidationException;
import ru.megamarket.service.ShopUnitService;
import ru.megamarket.openapi.OpenApiExamples;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.time.Instant;
import java.util.UUID;

@Validated
@RestController
@ApiResponse(
        responseCode = "400",
        description = "Невалидная схема документа или входные данные не верны.",
        content = {@Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = OpenApiExamples.VALIDATION_FAILED_ERROR),
                schema = @Schema(implementation = Error.class))})
@RequiredArgsConstructor
public class MegaMarketController {

    private final ShopUnitService shopUnitService;

    @Operation(description = "Импортирует новые товары и/или категории. Товары/категории импортированные повторно обновляют текущие." +
            " Изменение типа элемента с товара на категорию или с категории на товар не допускается." +
            " Порядок элементов в запросе является произвольным.")
    @ApiResponse(
            responseCode = "200",
            description = "Вставка или обновление прошли успешно",
            content = {@Content})
    @Tag(name = "Базовые задачи")
    @PostMapping("imports")
    public ResponseEntity<?> addItem(@RequestBody(required = false) @Valid ShopUnitImportRequest newShopUnitImportRequest) {
        try {
            shopUnitService.importItems(newShopUnitImportRequest.getItems(), newShopUnitImportRequest.getUpdateDate());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CustomValidationException exception) {
            throw new ValidationException(exception.getMessage());
        }
    }

    @Operation(description = "Удалить элемент по идентификатору." +
            " При удалении категории удаляются все дочерние элементы." +
            " Доступ к статистике (истории обновлений) удаленного элемента невозможен.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Удаление прошло успешно",
                    content = {@Content}),
            @ApiResponse(
                    responseCode = "404",
                    description = "Категория/товар не найден.",
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = OpenApiExamples.ITEM_NOT_FOUND_ERROR),
                            schema = @Schema(implementation = Error.class))})})
    @Parameter(in = ParameterIn.PATH,
            name = "id",
            description = "Идентификатор элемента",
            example = OpenApiExamples.ID)
    @Tag(name = "Базовые задачи")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteItem(@Valid @PathVariable @Schema(example = OpenApiExamples.ID) UUID id) {
        shopUnitService.removeItem(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(description = "Получить информацию об элементе по идентификатору." +
            " При получении информации о категории также предоставляется информация о её дочерних элементах.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация об элементе",
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ShopUnit.class))}),
            @ApiResponse(
                    responseCode = "404",
                    description = "Категория/товар не найден.",
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = OpenApiExamples.ITEM_NOT_FOUND_ERROR),
                            schema = @Schema(implementation = Error.class))})})
    @Parameter(in = ParameterIn.PATH,
            name = "id",
            description = "Идентификатор элемента",
            example = OpenApiExamples.ID)
    @Tag(name = "Базовые задачи")
    @GetMapping("/nodes/{id}")
    public ResponseEntity<?> getItem(@Valid @PathVariable @Schema(example = OpenApiExamples.ID) UUID id) {
        return new ResponseEntity<>(shopUnitService.findItem(id), HttpStatus.OK);
    }

    @Operation(description = "Получение списка товаров," +
            " цена которых была обновлена за последние 24 часа включительно [now() - 24h, now()] от времени переданном в запросе.")
    @ApiResponse(
            responseCode = "200",
            description = "Список товаров, цена которых была обновлена",
            content = {@Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ShopUnitStatisticResponse.class))})
    @Parameter(in = ParameterIn.QUERY,
            description = "Дата и время запроса.",
            name = "date",
            required = true,
            example = OpenApiExamples.DATE)
    @Tag(name = "Дополнительные задачи")
    @GetMapping("/sales")
    public ResponseEntity<?> getSales(@Schema(example = OpenApiExamples.DATE) @RequestParam Instant date) {
        ShopUnitStatisticResponse results = shopUnitService.findSales(date);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @Operation(description = "Получение статистики (истории обновлений) по товару/категории за заданный полуинтервал.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Статистика по элементу.",
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ShopUnitStatisticResponse.class))}),
            @ApiResponse(
                    responseCode = "404",
                    description = "Категория/товар не найден.",
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = OpenApiExamples.ITEM_NOT_FOUND_ERROR),
                            schema = @Schema(implementation = Error.class))})})
    @Parameters(value = {
            @Parameter(in = ParameterIn.QUERY,
                    description = "Дата и время начала интервала.",
                    name = "dateStart"),
            @Parameter(in = ParameterIn.QUERY,
                    description = "Дата и время конца интервала.",
                    name = "dateEnd"),
            @Parameter(in = ParameterIn.PATH,
                    description = "UUID товара/категории для которой будет отображаться статистика",
                    name = "id")
    })
    @Tag(name = "Дополнительные задачи")
    @GetMapping("/node/{id}/statistic")
    public ResponseEntity<?> getStatistic(@Schema(example = OpenApiExamples.ID) @Valid @PathVariable UUID id,
                                          @Schema(example = OpenApiExamples.DATE) @RequestParam(required = false) Instant dateStart,
                                          @Schema(example = OpenApiExamples.DATE) @RequestParam(required = false) Instant dateEnd) {
        return new ResponseEntity<>(shopUnitService.getStatistic(id, dateStart, dateEnd), HttpStatus.OK);
    }
}