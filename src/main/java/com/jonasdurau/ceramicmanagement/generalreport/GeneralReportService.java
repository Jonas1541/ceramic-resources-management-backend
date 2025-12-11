package com.jonasdurau.ceramicmanagement.generalreport;

import java.math.BigDecimal;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.batch.BatchRepository;
import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.DryingSessionRepository;
import com.jonasdurau.ceramicmanagement.glaze.transaction.GlazeTransactionRepository;
import com.jonasdurau.ceramicmanagement.product.transaction.ProductTransactionRepository;
import com.jonasdurau.ceramicmanagement.resource.transaction.ResourceTransactionRepository;
import com.jonasdurau.ceramicmanagement.shared.dto.MonthReportDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.enums.TransactionType;

@Service
public class GeneralReportService {

    @Autowired
    private ResourceTransactionRepository resourceTransactionRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private GlazeTransactionRepository glazeTransactionRepository;

    @Autowired
    private ProductTransactionRepository productTransactionRepository;

    @Autowired
    private DryingSessionRepository dryingSessionRepository;

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public List<YearReportDTO> generalYearlyReport() {
        ZoneId zone = ZoneId.systemDefault();
        Map<Integer, Map<Month, BigDecimal>> inputMap = new HashMap<>();
        Map<Integer, Map<Month, BigDecimal>> outputMap = new HashMap<>();

        resourceTransactionRepository.findAll().stream()
                .filter(rt -> rt.getType() == TransactionType.INCOMING)
                .forEach(rt -> {
                    ZonedDateTime zdt = rt.getCreatedAt().atZone(zone);
                    int year = zdt.getYear();
                    Month month = zdt.getMonth();
                    BigDecimal cost = rt.getCostAtTime() != null ? rt.getCostAtTime() : BigDecimal.ZERO;
                    addToMap(inputMap, year, month, cost);
                });

        batchRepository.findAll().forEach(batch -> {
            ZonedDateTime zdt = batch.getCreatedAt().atZone(zone);
            int year = zdt.getYear();
            Month month = zdt.getMonth();
            BigDecimal waterCost = batch.getBatchTotalWaterCostAtTime() != null ? batch.getBatchTotalWaterCostAtTime()
                    : BigDecimal.ZERO;
            BigDecimal machineCost = batch.getMachinesEnergyConsumptionCostAtTime() != null
                    ? batch.getMachinesEnergyConsumptionCostAtTime()
                    : BigDecimal.ZERO;
            BigDecimal cost = waterCost.add(machineCost);
            addToMap(inputMap, year, month, cost);
        });

        glazeTransactionRepository.findAll().stream()
                .filter(gt -> gt.getType() == TransactionType.INCOMING)
                .forEach(gt -> {
                    ZonedDateTime zdt = gt.getCreatedAt().atZone(zone);
                    int year = zdt.getYear();
                    Month month = zdt.getMonth();
                    BigDecimal cost = gt.getMachineEnergyConsumptionCostAtTime() != null
                            ? gt.getMachineEnergyConsumptionCostAtTime()
                            : BigDecimal.ZERO;
                    addToMap(inputMap, year, month, cost);
                });

        dryingSessionRepository.findAll().forEach(ds -> {
            ZonedDateTime zdt = ds.getCreatedAt().atZone(zone);
            int year = zdt.getYear();
            Month month = zdt.getMonth();
            BigDecimal cost = ds.getCostAtTime() != null ? ds.getCostAtTime() : BigDecimal.ZERO;
            addToMap(inputMap, year, month, cost);
        });

        productTransactionRepository.findAll().forEach(pt -> {
            ZonedDateTime zdt = pt.getCreatedAt().atZone(zone);
            int year = zdt.getYear();
            Month month = zdt.getMonth();

            BigDecimal cost = pt.getTotalCost() != null ? pt.getTotalCost() : BigDecimal.ZERO;
            addToMap(inputMap, year, month, cost);

            BigDecimal profit = pt.getProfit() != null ? pt.getProfit() : BigDecimal.ZERO;
            addToMap(outputMap, year, month, profit);
        });

        Set<Integer> allYears = new HashSet<>();
        allYears.addAll(inputMap.keySet());
        allYears.addAll(outputMap.keySet());

        List<YearReportDTO> reports = new ArrayList<>();
        for (Integer year : allYears) {
            YearReportDTO yearReport = new YearReportDTO(year);
            BigDecimal totalIncomingCost = BigDecimal.ZERO;
            BigDecimal totalOutgoingProfit = BigDecimal.ZERO;
            for (Month m : Month.values()) {
                BigDecimal monthInput = inputMap.getOrDefault(year, Collections.emptyMap()).getOrDefault(m,
                        BigDecimal.ZERO);
                BigDecimal monthOutput = outputMap.getOrDefault(year, Collections.emptyMap()).getOrDefault(m,
                        BigDecimal.ZERO);
                totalIncomingCost = totalIncomingCost.add(monthInput);
                totalOutgoingProfit = totalOutgoingProfit.add(monthOutput);
                MonthReportDTO monthDto = new MonthReportDTO();
                monthDto.setMonthName(m.getDisplayName(TextStyle.SHORT, Locale.getDefault()));
                monthDto.setIncomingQty(0.0);
                monthDto.setIncomingCost(monthInput);
                monthDto.setOutgoingQty(0.0);
                monthDto.setOutgoingProfit(monthOutput);
                yearReport.getMonths().add(monthDto);
            }
            yearReport.setTotalIncomingQty(0.0);
            yearReport.setTotalIncomingCost(totalIncomingCost);
            yearReport.setTotalOutgoingQty(0.0);
            yearReport.setTotalOutgoingProfit(totalOutgoingProfit);
            reports.add(yearReport);
        }

        reports.sort((a, b) -> b.getYear() - a.getYear());
        return reports;
    }

    private void addToMap(Map<Integer, Map<Month, BigDecimal>> map, int year, Month month, BigDecimal value) {
        map.computeIfAbsent(year, y -> new HashMap<>())
                .merge(month, value, BigDecimal::add);
    }
}
