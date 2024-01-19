package ru.main.ui_reporter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableEntity {
    private Integer number;
    private String name;
    private Long id;
}
