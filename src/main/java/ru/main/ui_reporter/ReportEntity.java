package ru.main.ui_reporter;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ReportEntity {
    private LocalDate date;
    private String projectName;
    private Double hours;
    private Long id;
    private String description;
    private String action;
    private Boolean extraHours;
}
