package com.onurbcd.cli.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onurbcd.cli.annotation.Helper;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.enums.Codeable;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.formatter.BigDecimalFormatter;
import com.onurbcd.cli.formatter.CodeableFormatter;
import com.onurbcd.cli.formatter.LocalDateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.shell.table.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.onurbcd.cli.util.Constant.*;

@Helper
@RequiredArgsConstructor
public class ShellHelper {

    private final ObjectMapper eruMapper;
    private final LocalDateTimeFormatter localDateTimeFormatter;
    private final BigDecimalFormatter bigDecimalFormatter;
    private final CodeableFormatter codeableFormatter;

    public String printJson(Dtoable dtoable) throws JsonProcessingException {
        return eruMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoable);
    }

    public String printTable(Page<Dtoable> page, EruTable table) {
        var tableBuilder = new TableBuilder(new BeanListTableModel<>(page.getContent(), table.getHeaders()))
                .addFullBorder(BorderStyle.fancy_heavy)
                .on(CellMatchers.ofType(LocalDateTime.class)).addFormatter(localDateTimeFormatter)
                .on(CellMatchers.ofType(BigDecimal.class)).addFormatter(bigDecimalFormatter)
                .on(CellMatchers.ofType(Number.class)).addAligner(SimpleHorizontalAligner.right)
                .on(CellMatchers.ofType(Codeable.class)).addFormatter(codeableFormatter)
                .on(CellMatchers.row(0)).addAligner(SimpleHorizontalAligner.center);

        var pageInfo = PAGE_INFO.formatted(page.getNumberOfElements(), page.getNumber() + 1, page.getTotalElements(),
                page.getTotalPages());

        return tableBuilder.build().render(1000) + pageInfo;
    }

    public <T> String printTable(Iterable<T> iterable, EruTable table) {
        return new TableBuilder(new BeanListTableModel<>(iterable, table.getHeaders()))
                .addFullBorder(BorderStyle.fancy_heavy)
                .on(CellMatchers.ofType(BigDecimal.class)).addFormatter(bigDecimalFormatter)
                .on(CellMatchers.ofType(Number.class)).addAligner(SimpleHorizontalAligner.right)
                .on(CellMatchers.ofType(Codeable.class)).addFormatter(codeableFormatter)
                .on(CellMatchers.row(0)).addAligner(SimpleHorizontalAligner.center)
                .build()
                .render(1000);
    }

    public String error(String message) {
        return ANSI_RED + message + ANSI_RESET;
    }

    public String success(String message) {
        return ANSI_GREEN + message + ANSI_RESET;
    }

    public String warning(String message) {
        return ANSI_YELLOW + message + ANSI_RESET;
    }
}
