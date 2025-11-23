package org.example.springboot_backend.service;

import com.google.analytics.data.v1beta.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoogleAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleAnalyticsService.class);
    
    @Autowired
    private BetaAnalyticsDataClient analyticsDataClient;
    
    @Value("${google.analytics.property.id:510032575}")
    private String propertyId;
    
    // ==================== MÉTRICAS EXISTENTES ====================
    
    public AnalyticsStats getAnalyticsStats() {
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = buildBasicRequest(property, "30daysAgo", "today",
                    Arrays.asList("activeUsers", "newUsers", "sessions", "screenPageViews", 
                                 "averageSessionDuration", "bounceRate", "engagementRate"));
            
            RunReportResponse response = analyticsDataClient.runReport(request);
            AnalyticsStats stats = new AnalyticsStats();
            
            if (!response.getRowsList().isEmpty()) {
                Row row = response.getRowsList().get(0);
                List<MetricValue> metrics = row.getMetricValuesList();
                
                stats.setActiveUsers(parseMetricValue(metrics, 0));
                stats.setNewUsers(parseMetricValue(metrics, 1));
                stats.setSessions(parseMetricValue(metrics, 2));
                stats.setPageViews(parseMetricValue(metrics, 3));
                stats.setAvgSessionDuration(parseDoubleMetricValue(metrics, 4));
                stats.setBounceRate(parseDoubleMetricValue(metrics, 5));
                stats.setEngagementRate(parseDoubleMetricValue(metrics, 6));
            }
            
            stats.setUserGrowth(calculateGrowth(property, "activeUsers"));
            return stats;
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas", e);
            throw e;
        }
    }
    
    public List<DailyMetric> getDailyUsers(int days) {
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(property)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate(days + "daysAgo")
                            .setEndDate("today"))
                    .addDimensions(Dimension.newBuilder().setName("date"))
                    .addMetrics(Metric.newBuilder().setName("activeUsers"))
                    .addMetrics(Metric.newBuilder().setName("newUsers"))
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .addOrderBys(OrderBy.newBuilder()
                            .setDimension(OrderBy.DimensionOrderBy.newBuilder()
                                    .setDimensionName("date")
                                    .setOrderType(OrderBy.DimensionOrderBy.OrderType.NUMERIC)))
                    .build();
            
            RunReportResponse response = analyticsDataClient.runReport(request);
            return response.getRowsList().stream()
                    .map(row -> {
                        DailyMetric metric = new DailyMetric();
                        metric.setDate(formatDate(row.getDimensionValues(0).getValue()));
                        metric.setUsers(Long.parseLong(row.getMetricValues(0).getValue()));
                        metric.setNewUsers(Long.parseLong(row.getMetricValues(1).getValue()));
                        metric.setSessions(Long.parseLong(row.getMetricValues(2).getValue()));
                        return metric;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo usuarios diarios", e);
            return Collections.emptyList();
        }
    }
    
    public List<PageMetric> getTopPages(int limit) {
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(property)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate("30daysAgo")
                            .setEndDate("today"))
                    .addDimensions(Dimension.newBuilder().setName("pageTitle"))
                    .addDimensions(Dimension.newBuilder().setName("pagePath"))
                    .addMetrics(Metric.newBuilder().setName("screenPageViews"))
                    .addMetrics(Metric.newBuilder().setName("averageSessionDuration"))
                    .addMetrics(Metric.newBuilder().setName("bounceRate"))
                    .setLimit(limit)
                    .addOrderBys(OrderBy.newBuilder()
                            .setMetric(OrderBy.MetricOrderBy.newBuilder()
                                    .setMetricName("screenPageViews"))
                            .setDesc(true))
                    .build();
            
            RunReportResponse response = analyticsDataClient.runReport(request);
            return response.getRowsList().stream()
                    .map(row -> {
                        PageMetric metric = new PageMetric();
                        metric.setPageTitle(row.getDimensionValues(0).getValue());
                        metric.setPagePath(row.getDimensionValues(1).getValue());
                        metric.setViews(Long.parseLong(row.getMetricValues(0).getValue()));
                        metric.setAvgDuration(Double.parseDouble(row.getMetricValues(1).getValue()));
                        metric.setBounceRate(Double.parseDouble(row.getMetricValues(2).getValue()));
                        return metric;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo páginas", e);
            return Collections.emptyList();
        }
    }
    
    public List<DeviceMetric> getDeviceStats() {
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(property)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate("30daysAgo")
                            .setEndDate("today"))
                    .addDimensions(Dimension.newBuilder().setName("deviceCategory"))
                    .addMetrics(Metric.newBuilder().setName("activeUsers"))
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .addMetrics(Metric.newBuilder().setName("averageSessionDuration"))
                    .build();
            
            RunReportResponse response = analyticsDataClient.runReport(request);
            return response.getRowsList().stream()
                    .map(row -> {
                        DeviceMetric metric = new DeviceMetric();
                        metric.setDeviceType(row.getDimensionValues(0).getValue());
                        metric.setUsers(Long.parseLong(row.getMetricValues(0).getValue()));
                        metric.setSessions(Long.parseLong(row.getMetricValues(1).getValue()));
                        metric.setAvgDuration(Double.parseDouble(row.getMetricValues(2).getValue()));
                        return metric;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo dispositivos", e);
            return Collections.emptyList();
        }
    }
    
    public List<LocationMetric> getTopLocations(int limit) {
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(property)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate("30daysAgo")
                            .setEndDate("today"))
                    .addDimensions(Dimension.newBuilder().setName("country"))
                    .addDimensions(Dimension.newBuilder().setName("city"))
                    .addMetrics(Metric.newBuilder().setName("activeUsers"))
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .setLimit(limit)
                    .addOrderBys(OrderBy.newBuilder()
                            .setMetric(OrderBy.MetricOrderBy.newBuilder()
                                    .setMetricName("activeUsers"))
                            .setDesc(true))
                    .build();
            
            RunReportResponse response = analyticsDataClient.runReport(request);
            return response.getRowsList().stream()
                    .map(row -> {
                        LocationMetric metric = new LocationMetric();
                        metric.setCountry(row.getDimensionValues(0).getValue());
                        metric.setCity(row.getDimensionValues(1).getValue());
                        metric.setUsers(Long.parseLong(row.getMetricValues(0).getValue()));
                        metric.setSessions(Long.parseLong(row.getMetricValues(1).getValue()));
                        return metric;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo ubicaciones", e);
            return Collections.emptyList();
        }
    }
    
    // ==================== NUEVAS MÉTRICAS ====================
    
    /**
     * Obtiene tráfico por hora del día
     */
    public List<HourlyMetric> getTrafficByHour() {
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(property)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate("7daysAgo")
                            .setEndDate("today"))
                    .addDimensions(Dimension.newBuilder().setName("hour"))
                    .addMetrics(Metric.newBuilder().setName("activeUsers"))
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .addOrderBys(OrderBy.newBuilder()
                            .setDimension(OrderBy.DimensionOrderBy.newBuilder()
                                    .setDimensionName("hour")
                                    .setOrderType(OrderBy.DimensionOrderBy.OrderType.NUMERIC)))
                    .build();
            
            RunReportResponse response = analyticsDataClient.runReport(request);
            return response.getRowsList().stream()
                    .map(row -> {
                        HourlyMetric metric = new HourlyMetric();
                        metric.setHour(Integer.parseInt(row.getDimensionValues(0).getValue()));
                        metric.setUsers(Long.parseLong(row.getMetricValues(0).getValue()));
                        metric.setSessions(Long.parseLong(row.getMetricValues(1).getValue()));
                        return metric;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo tráfico por hora", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Obtiene los eventos más importantes
     */
    public List<EventMetric> getTopEvents(int limit) {
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(property)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate("30daysAgo")
                            .setEndDate("today"))
                    .addDimensions(Dimension.newBuilder().setName("eventName"))
                    .addMetrics(Metric.newBuilder().setName("eventCount"))
                    .addMetrics(Metric.newBuilder().setName("eventCountPerUser"))
                    .setLimit(limit)
                    .addOrderBys(OrderBy.newBuilder()
                            .setMetric(OrderBy.MetricOrderBy.newBuilder()
                                    .setMetricName("eventCount"))
                            .setDesc(true))
                    .build();
            
            RunReportResponse response = analyticsDataClient.runReport(request);
            return response.getRowsList().stream()
                    .map(row -> {
                        EventMetric metric = new EventMetric();
                        metric.setEventName(row.getDimensionValues(0).getValue());
                        metric.setEventCount(Long.parseLong(row.getMetricValues(0).getValue()));
                        metric.setEventCountPerUser(Double.parseDouble(row.getMetricValues(1).getValue()));
                        return metric;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo eventos", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Obtiene fuentes de tráfico (de dónde vienen los usuarios)
     */
    public List<SourceMetric> getTrafficSources(int limit) {
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(property)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate("30daysAgo")
                            .setEndDate("today"))
                    .addDimensions(Dimension.newBuilder().setName("sessionSource"))
                    .addDimensions(Dimension.newBuilder().setName("sessionMedium"))
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .addMetrics(Metric.newBuilder().setName("activeUsers"))
                    .addMetrics(Metric.newBuilder().setName("newUsers"))
                    .setLimit(limit)
                    .addOrderBys(OrderBy.newBuilder()
                            .setMetric(OrderBy.MetricOrderBy.newBuilder()
                                    .setMetricName("sessions"))
                            .setDesc(true))
                    .build();
            
            RunReportResponse response = analyticsDataClient.runReport(request);
            return response.getRowsList().stream()
                    .map(row -> {
                        SourceMetric metric = new SourceMetric();
                        metric.setSource(row.getDimensionValues(0).getValue());
                        metric.setMedium(row.getDimensionValues(1).getValue());
                        metric.setSessions(Long.parseLong(row.getMetricValues(0).getValue()));
                        metric.setUsers(Long.parseLong(row.getMetricValues(1).getValue()));
                        metric.setNewUsers(Long.parseLong(row.getMetricValues(2).getValue()));
                        return metric;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo fuentes de tráfico", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Obtiene navegadores más usados
     */
    public List<BrowserMetric> getTopBrowsers(int limit) {
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(property)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate("30daysAgo")
                            .setEndDate("today"))
                    .addDimensions(Dimension.newBuilder().setName("browser"))
                    .addMetrics(Metric.newBuilder().setName("activeUsers"))
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .setLimit(limit)
                    .addOrderBys(OrderBy.newBuilder()
                            .setMetric(OrderBy.MetricOrderBy.newBuilder()
                                    .setMetricName("activeUsers"))
                            .setDesc(true))
                    .build();
            
            RunReportResponse response = analyticsDataClient.runReport(request);
            return response.getRowsList().stream()
                    .map(row -> {
                        BrowserMetric metric = new BrowserMetric();
                        metric.setBrowserName(row.getDimensionValues(0).getValue());
                        metric.setUsers(Long.parseLong(row.getMetricValues(0).getValue()));
                        metric.setSessions(Long.parseLong(row.getMetricValues(1).getValue()));
                        return metric;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo navegadores", e);
            return Collections.emptyList();
        }
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    private RunReportRequest buildBasicRequest(String property, String startDate, 
                                               String endDate, List<String> metricNames) {
        RunReportRequest.Builder builder = RunReportRequest.newBuilder()
                .setProperty(property)
                .addDateRanges(DateRange.newBuilder()
                        .setStartDate(startDate)
                        .setEndDate(endDate));
        
        metricNames.forEach(name -> builder.addMetrics(Metric.newBuilder().setName(name)));
        return builder.build();
    }
    
    private long parseMetricValue(List<MetricValue> metrics, int index) {
        try {
            if (metrics.size() > index) {
                return Long.parseLong(metrics.get(index).getValue());
            }
        } catch (NumberFormatException e) {
            logger.warn("Error parsing metric at index {}", index);
        }
        return 0L;
    }
    
    private double parseDoubleMetricValue(List<MetricValue> metrics, int index) {
        try {
            if (metrics.size() > index) {
                return Double.parseDouble(metrics.get(index).getValue());
            }
        } catch (NumberFormatException e) {
            logger.warn("Error parsing double metric at index {}", index);
        }
        return 0.0;
    }
    
    private double calculateGrowth(String property, String metricName) {
        try {
            RunReportRequest currentRequest = buildBasicRequest(property, "30daysAgo", "today",
                    Collections.singletonList(metricName));
            RunReportResponse currentResponse = analyticsDataClient.runReport(currentRequest);
            
            RunReportRequest previousRequest = buildBasicRequest(property, "60daysAgo", "31daysAgo",
                    Collections.singletonList(metricName));
            RunReportResponse previousResponse = analyticsDataClient.runReport(previousRequest);
            
            if (!currentResponse.getRowsList().isEmpty() && !previousResponse.getRowsList().isEmpty()) {
                long current = Long.parseLong(currentResponse.getRows(0).getMetricValues(0).getValue());
                long previous = Long.parseLong(previousResponse.getRows(0).getMetricValues(0).getValue());
                
                if (previous > 0) {
                    return ((double) (current - previous) / previous) * 100;
                }
            }
        } catch (Exception e) {
            logger.warn("Error calculating growth", e);
        }
        return 0.0;
    }
    
    private String formatDate(String dateString) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(dateString, inputFormatter);
            return date.format(outputFormatter);
        } catch (Exception e) {
            return dateString;
        }
    }
    
    // ==================== CLASES DTO ====================
    
    public static class AnalyticsStats {
        private long activeUsers;
        private long newUsers;
        private long sessions;
        private long pageViews;
        private double userGrowth;
        private double avgSessionDuration;
        private double bounceRate;
        private double engagementRate;
        
        // Getters y Setters
        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
        
        public long getNewUsers() { return newUsers; }
        public void setNewUsers(long newUsers) { this.newUsers = newUsers; }
        
        public long getSessions() { return sessions; }
        public void setSessions(long sessions) { this.sessions = sessions; }
        
        public long getPageViews() { return pageViews; }
        public void setPageViews(long pageViews) { this.pageViews = pageViews; }
        
        public double getUserGrowth() { return userGrowth; }
        public void setUserGrowth(double userGrowth) { this.userGrowth = userGrowth; }
        
        public double getAvgSessionDuration() { return avgSessionDuration; }
        public void setAvgSessionDuration(double avgSessionDuration) { this.avgSessionDuration = avgSessionDuration; }
        
        public double getBounceRate() { return bounceRate; }
        public void setBounceRate(double bounceRate) { this.bounceRate = bounceRate; }
        
        public double getEngagementRate() { return engagementRate; }
        public void setEngagementRate(double engagementRate) { this.engagementRate = engagementRate; }
    }
    
    public static class DailyMetric {
        private String date;
        private long users;
        private long newUsers;
        private long sessions;
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public long getUsers() { return users; }
        public void setUsers(long users) { this.users = users; }
        
        public long getNewUsers() { return newUsers; }
        public void setNewUsers(long newUsers) { this.newUsers = newUsers; }
        
        public long getSessions() { return sessions; }
        public void setSessions(long sessions) { this.sessions = sessions; }
    }
    
    public static class PageMetric {
        private String pageTitle;
        private String pagePath;
        private long views;
        private double avgDuration;
        private double bounceRate;
        
        public String getPageTitle() { return pageTitle; }
        public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }
        
        public String getPagePath() { return pagePath; }
        public void setPagePath(String pagePath) { this.pagePath = pagePath; }
        
        public long getViews() { return views; }
        public void setViews(long views) { this.views = views; }
        
        public double getAvgDuration() { return avgDuration; }
        public void setAvgDuration(double avgDuration) { this.avgDuration = avgDuration; }
        
        public double getBounceRate() { return bounceRate; }
        public void setBounceRate(double bounceRate) { this.bounceRate = bounceRate; }
    }
    
    public static class DeviceMetric {
        private String deviceType;
        private long users;
        private long sessions;
        private double avgDuration;
        
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        
        public long getUsers() { return users; }
        public void setUsers(long users) { this.users = users; }
        
        public long getSessions() { return sessions; }
        public void setSessions(long sessions) { this.sessions = sessions; }
        
        public double getAvgDuration() { return avgDuration; }
        public void setAvgDuration(double avgDuration) { this.avgDuration = avgDuration; }
    }
    
    public static class LocationMetric {
        private String country;
        private String city;
        private long users;
        private long sessions;
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public long getUsers() { return users; }
        public void setUsers(long users) { this.users = users; }
        
        public long getSessions() { return sessions; }
        public void setSessions(long sessions) { this.sessions = sessions; }
    }
    
    public static class HourlyMetric {
        private int hour;
        private long users;
        private long sessions;
        
        public int getHour() { return hour; }
        public void setHour(int hour) { this.hour = hour; }
        
        public long getUsers() { return users; }
        public void setUsers(long users) { this.users = users; }
        
        public long getSessions() { return sessions; }
        public void setSessions(long sessions) { this.sessions = sessions; }
    }
    
    public static class EventMetric {
        private String eventName;
        private long eventCount;
        private double eventCountPerUser;
        
        public String getEventName() { return eventName; }
        public void setEventName(String eventName) { this.eventName = eventName; }
        
        public long getEventCount() { return eventCount; }
        public void setEventCount(long eventCount) { this.eventCount = eventCount; }
        
        public double getEventCountPerUser() { return eventCountPerUser; }
        public void setEventCountPerUser(double eventCountPerUser) { this.eventCountPerUser = eventCountPerUser; }
    }
    
    public static class SourceMetric {
        private String source;
        private String medium;
        private long sessions;
        private long users;
        private long newUsers;
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public String getMedium() { return medium; }
        public void setMedium(String medium) { this.medium = medium; }
        
        public long getSessions() { return sessions; }
        public void setSessions(long sessions) { this.sessions = sessions; }
        
        public long getUsers() { return users; }
        public void setUsers(long users) { this.users = users; }
        
        public long getNewUsers() { return newUsers; }
        public void setNewUsers(long newUsers) { this.newUsers = newUsers; }
    }
    
    public static class BrowserMetric {
        private String browserName;
        private long users;
        private long sessions;
        
        public String getBrowserName() { return browserName; }
        public void setBrowserName(String browserName) { this.browserName = browserName; }
        
        public long getUsers() { return users; }
        public void setUsers(long users) { this.users = users; }
        
        public long getSessions() { return sessions; }
        public void setSessions(long sessions) { this.sessions = sessions; }
    }
}