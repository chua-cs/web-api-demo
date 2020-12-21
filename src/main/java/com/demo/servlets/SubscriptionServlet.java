package com.demo.servlets;

import com.demo.entities.Subscription;
import com.demo.enums.SubscriptionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chua.cs@outlook.com
 * @since 2020/12/17
 */
@WebServlet(name = "SubscriptionServlet", urlPatterns = {"/subscription"})
public class SubscriptionServlet extends HttpServlet {

    private static final int MAX_MONTHS = 3;
    private static final String INVOICE_DATE_FORMAT = "dd/MM/yyyy";
    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern(INVOICE_DATE_FORMAT);

    private static final String DEFAULT_JSON_RESPONSE =
            "{\"status\":\"abnormal\", \"message\":\"Parameters and values are required.\"}";

    /**
     * GET - Request
     *
     * @param request {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        Subscription subscription = processRequestParameters(request);

        if (isValid(subscription)) {
            performJsonResponse(response, convertToJsonString(subscription));
        } else {
            performJsonResponse(response, DEFAULT_JSON_RESPONSE);
        }
    }

    /**
     * Read the parameters and values from the servlet request
     *
     * @param request {@link HttpServletRequest}
     * @return
     */
    private Subscription processRequestParameters(HttpServletRequest request) {
        Subscription subscription = new Subscription();

        // Param key: amount
        if (StringUtils.isNotBlank(request.getParameter("amount"))) {
            subscription.setAmount(new BigDecimal(request.getParameter("amount")));
        }

        // Param key: subscriptionType
        if (StringUtils.isNotBlank(request.getParameter("subscriptionType"))) {
            String type = request.getParameter("subscriptionType");
            if (EnumUtils.isValidEnumIgnoreCase(SubscriptionType.class, type)) {
                subscription.setType(SubscriptionType.valueOf(type.toUpperCase()));
            }
        }

        // Param key: startDate & endDate
        if (StringUtils.isNotBlank(request.getParameter("startDate"))
                && StringUtils.isNotBlank(request.getParameter("endDate"))) {
            String startDateStr = request.getParameter("startDate");
            subscription.setStartDate(LocalDate.parse(startDateStr, dtFormatter));

            String endDateStr = request.getParameter("endDate");
            subscription.setEndDate(LocalDate.parse(endDateStr, dtFormatter));

            if (subscription.getStartDate().isAfter(subscription.getEndDate())
                    || ChronoUnit.MONTHS.between(subscription.getStartDate(), subscription.getEndDate()) >= 3) {
                subscription.setEndDate(subscription.getStartDate().plusMonths(MAX_MONTHS));
            }
        }

        // Conditional check for subscriptionType
        if (SubscriptionType.WEEKLY.equals(subscription.getType())
                && StringUtils.isNotBlank(request.getParameter("dayOfWeek"))) {
            String dow = request.getParameter("dayOfWeek");
            if (EnumUtils.isValidEnumIgnoreCase(DayOfWeek.class, dow)) {
                subscription.setDayOfWeek(DayOfWeek.valueOf(dow.toUpperCase()));
            }
        } else if (SubscriptionType.MONTHLY.equals(subscription.getType())) {
            String dom = request.getParameter("dayOfMonth");
            subscription.setDayOfMonth(Integer.parseInt(dom));
        }

        generateInvoiceDateList(subscription);

        return subscription;
    }

    /**
     * Generate the list of invoice date based on the subscription start-date and end-date.
     *
     * @param subscription {@link Subscription}
     */
    private void generateInvoiceDateList(Subscription subscription) {
        List<String> invoiceDateList = new ArrayList<>();

        if (null != subscription.getDayOfWeek()) {
            LocalDate firstDay = subscription.getStartDate().with(TemporalAdjusters.nextOrSame(subscription.getDayOfWeek()));
            for (LocalDate day = firstDay; day.isBefore(subscription.getEndDate()); day = day.plusWeeks(1)) {
                invoiceDateList.add(dtFormatter.format(day));
            }
        }

        if (SubscriptionType.MONTHLY.equals(subscription.getType())) {
            LocalDate firstDay = LocalDate.now().withDayOfMonth(subscription.getDayOfMonth());
            if (subscription.getDayOfMonth() < subscription.getStartDate().getDayOfMonth()) {
                firstDay = subscription.getStartDate().plusMonths(1).withDayOfMonth(subscription.getDayOfMonth());
            }
            for (LocalDate day = firstDay; day.isBefore(subscription.getEndDate()); day = day.plusMonths(1)) {
                invoiceDateList.add(dtFormatter.format(day));
            }
        }

        subscription.setInvoiceDateList(invoiceDateList);
    }

    /**
     * Perform validation on a {@link Subscription}
     *
     * @param subscription {@link Subscription}
     * @return <code>true</code> if key fields are not null, else <code>false</code>.
     */
    private boolean isValid(Subscription subscription) {
        if (null != subscription) {
            if (null != subscription.getAmount()
                    && null != subscription.getType()
                    && null != subscription.getStartDate()
                    && null != subscription.getEndDate()
            ) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * Convert an object into JSON String representation.
     *
     * @param obj {@link Object}
     * @return The JSON String representation of the object.
     */
    private String convertToJsonString(Object obj) {
        String jsonStr = "{}";
        try {
            jsonStr = new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .registerModule(new JavaTimeModule())
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonStr;
    }

    /**
     * Send out {@link HttpServletResponse} in JSON String format
     *
     * @param response {@link HttpServletResponse}
     * @param json JSON String
     */
    private void performJsonResponse(HttpServletResponse response, String json) {
        try (PrintWriter pw = response.getWriter()) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            pw.print(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
