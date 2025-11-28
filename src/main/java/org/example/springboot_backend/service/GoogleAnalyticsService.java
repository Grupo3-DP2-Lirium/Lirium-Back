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
    
    @Autowired(required = false)
    private BetaAnalyticsDataClient analyticsDataClient;
    
    @Value("${google.analytics.property.id:510032575}")
    private String propertyId;
    
    @Value("${google.analytics.enabled:true}")
    private boolean enabled;
    
    /**
     * Verifica si Google Analytics está disponible
     */
    private boolean isAvailable() {
        if (!enabled || analyticsDataClient == null) {
            logger.warn("Google Analytics no está disponible o está deshabilitado");
            return false;
        }
        return true;
    }
    
    // ==================== MÉTRICAS PRINCIPALES CON FILTRO DE TIEMPO ====================
    
    /**
     * Estadísticas generales con filtro de días
     */
    public AnalyticsStats getAnalyticsStats(int days) {
        if (!isAvailable()) {
            return new AnalyticsStats(); // Retorna stats vacías
        }
        
        try {
            String property = "properties/" + propertyId;
            String startDate = days + "daysAgo";
            
            RunReportRequest request = buildBasicRequest(property, startDate, "today",
                    Arrays.asList("activeUsers", "newUsers", "sessions", "screenPageViews", 
                                 "averageSessionDuration", "engagementRate", "userEngagementDuration"));
            
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
                stats.setEngagementRate(parseDoubleMetricValue(metrics, 5));
                stats.setTotalEngagementTime(parseDoubleMetricValue(metrics, 6));
            }
            
            // Calcular crecimiento comparando con el período anterior
            stats.setUserGrowth(calculateGrowth(property, "activeUsers", days));
            stats.setSessionGrowth(calculateGrowth(property, "sessions", days));
            
            return stats;
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas", e);
            throw e;
        }
    }
    
    /**
     * Usuarios por día con filtro
     */
    public List<DailyMetric> getDailyUsers(int days) {
        if (!isAvailable()) {
            return Collections.emptyList();
        }
        
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
                    .addMetrics(Metric.newBuilder().setName("userEngagementDuration"))
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
                        metric.setEngagementTime(Double.parseDouble(row.getMetricValues(3).getValue()));
                        return metric;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo usuarios diarios", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Dispositivos (útil y sin "not set" generalmente)
     */
    public List<DeviceMetric> getDeviceStats(int days) {
        if (!isAvailable()) {
            return Collections.emptyList();
        }
        
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(property)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate(days + "daysAgo")
                            .setEndDate("today"))
                    .addDimensions(Dimension.newBuilder().setName("deviceCategory"))
                    .addMetrics(Metric.newBuilder().setName("activeUsers"))
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .addMetrics(Metric.newBuilder().setName("averageSessionDuration"))
                    .addMetrics(Metric.newBuilder().setName("engagementRate"))
                    .build();
            
            RunReportResponse response = analyticsDataClient.runReport(request);
            return response.getRowsList().stream()
                    .map(row -> {
                        DeviceMetric metric = new DeviceMetric();
                        metric.setDeviceType(row.getDimensionValues(0).getValue());
                        metric.setUsers(Long.parseLong(row.getMetricValues(0).getValue()));
                        metric.setSessions(Long.parseLong(row.getMetricValues(1).getValue()));
                        metric.setAvgDuration(Double.parseDouble(row.getMetricValues(2).getValue()));
                        metric.setEngagementRate(Double.parseDouble(row.getMetricValues(3).getValue()));
                        return metric;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo dispositivos", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Ubicaciones principales - FILTRADO (sin "not set")
     */
    public List<LocationMetric> getTopLocations(int days, int limit) {
        if (!isAvailable()) {
            return Collections.emptyList();
        }
        
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(property)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate(days + "daysAgo")
                            .setEndDate("today"))
                    .addDimensions(Dimension.newBuilder().setName("city"))
                    .addDimensions(Dimension.newBuilder().setName("country"))
                    .addMetrics(Metric.newBuilder().setName("activeUsers"))
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .setLimit(limit * 2) // Pedimos más para filtrar
                    .addOrderBys(OrderBy.newBuilder()
                            .setMetric(OrderBy.MetricOrderBy.newBuilder()
                                    .setMetricName("activeUsers"))
                            .setDesc(true))
                    .build();
            
            RunReportResponse response = analyticsDataClient.runReport(request);
            return response.getRowsList().stream()
                    .limit(limit)
                    .map(row -> {
                        LocationMetric metric = new LocationMetric();
                        metric.setCity(row.getDimensionValues(0).getValue());
                        metric.setCountry(row.getDimensionValues(1).getValue());
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
    
    /**
     * Tráfico por hora del día
     */
    public List<HourlyMetric> getTrafficByHour(int days) {
        if (!isAvailable()) {
            return Collections.emptyList();
        }
        
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(property)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate(days + "daysAgo")
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
     * Eventos principales - FILTRADOS (solo eventos útiles)
     */
    public List<EventMetric> getTopEvents(int days, int limit) {
        if (!isAvailable()) {
            return Collections.emptyList();
        }
        
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(property)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate(days + "daysAgo")
                            .setEndDate("today"))
                    .addDimensions(Dimension.newBuilder().setName("eventName"))
                    .addMetrics(Metric.newBuilder().setName("eventCount"))
                    .addMetrics(Metric.newBuilder().setName("eventCountPerUser"))
                    .setLimit(50) // Pedimos más para filtrar
                    .addOrderBys(OrderBy.newBuilder()
                            .setMetric(OrderBy.MetricOrderBy.newBuilder()
                                    .setMetricName("eventCount"))
                            .setDesc(true))
                    .build();
            
            RunReportResponse response = analyticsDataClient.runReport(request);
            
            // Eventos a filtrar (eventos técnicos de Firebase que no aportan)
            Set<String> ignoredEvents = new HashSet<>(Arrays.asList(
                "screen_view", "user_engagement", "first_visit", "session_start",
                "first_open", "app_remove", "app_clear_data", "notification_dismiss",
                "notification_foreground", "notification_receive", "firebase_campaign"
            ));
            
            return response.getRowsList().stream()
                    .filter(row -> {
                        String eventName = row.getDimensionValues(0).getValue();
                        return !ignoredEvents.contains(eventName);
                    })
                    .limit(limit)
                    .map(row -> {
                        EventMetric metric = new EventMetric();
                        String eventName = row.getDimensionValues(0).getValue();
                        metric.setEventName(formatEventName(eventName));
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
     * Retención de usuarios (usuarios que regresan)
     */
    public RetentionMetric getUserRetention(int days) {
        if (!isAvailable()) {
            return new RetentionMetric();
        }
        
        try {
            String property = "properties/" + propertyId;
            
            // Total de usuarios
            RunReportRequest totalRequest = buildBasicRequest(property, days + "daysAgo", "today",
                    Collections.singletonList("activeUsers"));
            RunReportResponse totalResponse = analyticsDataClient.runReport(totalRequest);
            long totalUsers = totalResponse.getRowsList().isEmpty() ? 0 :
                    Long.parseLong(totalResponse.getRows(0).getMetricValues(0).getValue());
            
            // Usuarios nuevos
            RunReportRequest newRequest = buildBasicRequest(property, days + "daysAgo", "today",
                    Collections.singletonList("newUsers"));
            RunReportResponse newResponse = analyticsDataClient.runReport(newRequest);
            long newUsers = newResponse.getRowsList().isEmpty() ? 0 :
                    Long.parseLong(newResponse.getRows(0).getMetricValues(0).getValue());
            
            // Calcular usuarios que regresan
            long returningUsers = totalUsers - newUsers;
            double retentionRate = totalUsers > 0 ? (returningUsers * 100.0 / totalUsers) : 0;
            
            RetentionMetric retention = new RetentionMetric();
            retention.setTotalUsers(totalUsers);
            retention.setNewUsers(newUsers);
            retention.setReturningUsers(Math.max(0, returningUsers));
            retention.setRetentionRate(retentionRate);
            
            return retention;
        } catch (Exception e) {
            logger.error("Error calculando retención", e);
            return new RetentionMetric();
        }
    }
    
    /**
     * Engagement por día de la semana
     */
    public List<WeekdayMetric> getEngagementByWeekday(int days) {
        if (!isAvailable()) {
            return Collections.emptyList();
        }
        
        try {
            String property = "properties/" + propertyId;
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(property)
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate(days + "daysAgo")
                            .setEndDate("today"))
                    .addDimensions(Dimension.newBuilder().setName("dayOfWeek"))
                    .addMetrics(Metric.newBuilder().setName("activeUsers"))
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .addMetrics(Metric.newBuilder().setName("engagementRate"))
                    .addOrderBys(OrderBy.newBuilder()
                            .setDimension(OrderBy.DimensionOrderBy.newBuilder()
                                    .setDimensionName("dayOfWeek")
                                    .setOrderType(OrderBy.DimensionOrderBy.OrderType.NUMERIC)))
                    .build();
            
            RunReportResponse response = analyticsDataClient.runReport(request);
            
            // Nombres de días
            String[] dayNames = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
            
            return response.getRowsList().stream()
                    .map(row -> {
                        WeekdayMetric metric = new WeekdayMetric();
                        int dayIndex = Integer.parseInt(row.getDimensionValues(0).getValue());
                        metric.setDayOfWeek(dayNames[dayIndex]);
                        metric.setDayIndex(dayIndex);
                        metric.setUsers(Long.parseLong(row.getMetricValues(0).getValue()));
                        metric.setSessions(Long.parseLong(row.getMetricValues(1).getValue()));
                        metric.setEngagementRate(Double.parseDouble(row.getMetricValues(2).getValue()));
                        return metric;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error obteniendo engagement por día", e);
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
    
    private double calculateGrowth(String property, String metricName, int days) {
        try {
            // Período actual
            RunReportRequest currentRequest = buildBasicRequest(property, days + "daysAgo", "today",
                    Collections.singletonList(metricName));
            RunReportResponse currentResponse = analyticsDataClient.runReport(currentRequest);
            
            // Período anterior (mismo número de días hacia atrás)
            int previousStart = days * 2;
            int previousEnd = days + 1;
            RunReportRequest previousRequest = buildBasicRequest(property, 
                    previousStart + "daysAgo", previousEnd + "daysAgo",
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
    
    /**
     * Formatea nombres de eventos para que sean más legibles
     */
    private String formatEventName(String eventName) {
        // Convertir snake_case a Title Case
        return Arrays.stream(eventName.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
    
    // ==================== CLASES DTO ====================
    
    public static class AnalyticsStats {
        private long activeUsers;
        private long newUsers;
        private long sessions;
        private long pageViews;
        private double userGrowth;
        private double sessionGrowth;
        private double avgSessionDuration;
        private double engagementRate;
        private double totalEngagementTime;
        
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
        
        public double getSessionGrowth() { return sessionGrowth; }
        public void setSessionGrowth(double sessionGrowth) { this.sessionGrowth = sessionGrowth; }
        
        public double getAvgSessionDuration() { return avgSessionDuration; }
        public void setAvgSessionDuration(double avgSessionDuration) { this.avgSessionDuration = avgSessionDuration; }
        
        public double getEngagementRate() { return engagementRate; }
        public void setEngagementRate(double engagementRate) { this.engagementRate = engagementRate; }
        
        public double getTotalEngagementTime() { return totalEngagementTime; }
        public void setTotalEngagementTime(double totalEngagementTime) { this.totalEngagementTime = totalEngagementTime; }
    }
    
    public static class DailyMetric {
        private String date;
        private long users;
        private long newUsers;
        private long sessions;
        private double engagementTime;
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public long getUsers() { return users; }
        public void setUsers(long users) { this.users = users; }
        
        public long getNewUsers() { return newUsers; }
        public void setNewUsers(long newUsers) { this.newUsers = newUsers; }
        
        public long getSessions() { return sessions; }
        public void setSessions(long sessions) { this.sessions = sessions; }
        
        public double getEngagementTime() { return engagementTime; }
        public void setEngagementTime(double engagementTime) { this.engagementTime = engagementTime; }
    }
    
    public static class DeviceMetric {
        private String deviceType;
        private long users;
        private long sessions;
        private double avgDuration;
        private double engagementRate;
        
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        
        public long getUsers() { return users; }
        public void setUsers(long users) { this.users = users; }
        
        public long getSessions() { return sessions; }
        public void setSessions(long sessions) { this.sessions = sessions; }
        
        public double getAvgDuration() { return avgDuration; }
        public void setAvgDuration(double avgDuration) { this.avgDuration = avgDuration; }
        
        public double getEngagementRate() { return engagementRate; }
        public void setEngagementRate(double engagementRate) { this.engagementRate = engagementRate; }
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
    
    public static class RetentionMetric {
        private long totalUsers;
        private long newUsers;
        private long returningUsers;
        private double retentionRate;
        
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        
        public long getNewUsers() { return newUsers; }
        public void setNewUsers(long newUsers) { this.newUsers = newUsers; }
        
        public long getReturningUsers() { return returningUsers; }
        public void setReturningUsers(long returningUsers) { this.returningUsers = returningUsers; }
        
        public double getRetentionRate() { return retentionRate; }
        public void setRetentionRate(double retentionRate) { this.retentionRate = retentionRate; }
    }
    
    public static class WeekdayMetric {
        private String dayOfWeek;
        private int dayIndex;
        private long users;
        private long sessions;
        private double engagementRate;
        
        public String getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        
        public int getDayIndex() { return dayIndex; }
        public void setDayIndex(int dayIndex) { this.dayIndex = dayIndex; }
        
        public long getUsers() { return users; }
        public void setUsers(long users) { this.users = users; }
        
        public long getSessions() { return sessions; }
        public void setSessions(long sessions) { this.sessions = sessions; }
        
        public double getEngagementRate() { return engagementRate; }
        public void setEngagementRate(double engagementRate) { this.engagementRate = engagementRate; }
    }
}