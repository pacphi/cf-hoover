package io.pivotal.cfapp.domain.accounting.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
@JsonPropertyOrder({"report_time", "monthly_reports", "yearly_reports"})
public class AppUsageReport {

    @JsonProperty("report_time")
    private String reportTime;

    @Default
    @JsonProperty("monthly_reports")
    private List<AppUsageMonthly> monthlyReports = new ArrayList<>();

    @Default
    @JsonProperty("yearly_reports")
    private List<AppUsageYearly> yearlyReports = new ArrayList<>();

    @JsonCreator
    public AppUsageReport(
        @JsonProperty("report_time") String reportTime,
        @JsonProperty("monthly_reports") List<AppUsageMonthly> monthlyReports,
        @JsonProperty("yearly_reports") List<AppUsageYearly> yearlyReports) {
        this.reportTime = reportTime;
        this.monthlyReports = monthlyReports;
        this.yearlyReports = yearlyReports;
    }


    public static AppUsageReport aggregate(List<AppUsageReport> source) {
        AppUsageReportBuilder report = AppUsageReport.builder();
        List<AppUsageMonthly> monthlyReports = new CopyOnWriteArrayList<>();
        List<AppUsageYearly> yearlyReports = new CopyOnWriteArrayList<>();
        report.reportTime(LocalDateTime.now().toString());
        source.forEach(aur -> {
            if (monthlyReports.isEmpty()) {
                monthlyReports.addAll(aur.getMonthlyReports());
            } else {
                for (AppUsageMonthly mr: monthlyReports){
                    for (AppUsageMonthly smr: aur.getMonthlyReports()) {
                        if (!mr.combine(smr)) {
                            monthlyReports.add(smr);
                        }
                    }
                }
            }
            if (yearlyReports.isEmpty()) {
                yearlyReports.addAll(aur.getYearlyReports());
            } else {
                for (AppUsageYearly yr: yearlyReports){
                    for (AppUsageYearly syr: aur.getYearlyReports()) {
                        if (!yr.combine(syr)) {
                            yearlyReports.add(syr);
                        }
                    }
                }
            }
        });
        List<AppUsageMonthly> sortedMonthlyReports = new ArrayList<>();
        sortedMonthlyReports.addAll(monthlyReports);
        sortedMonthlyReports.sort(Comparator.comparing(AppUsageMonthly::getYearAndMonth));
        report.monthlyReports(sortedMonthlyReports);
        List<AppUsageYearly> sortedYearlyReports = new ArrayList<>();
        sortedYearlyReports.addAll(yearlyReports);
        sortedYearlyReports.sort(Comparator.comparing(AppUsageYearly::getYear));
        report.yearlyReports(sortedYearlyReports);
        return report.build();
    }
}