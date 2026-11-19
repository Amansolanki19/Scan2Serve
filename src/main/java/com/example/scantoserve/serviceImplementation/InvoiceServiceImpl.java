
package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.entity.*;
import com.example.scantoserve.exception.ResourceNotFoundException;
import com.example.scantoserve.repository.CustomerOrderRepo;
import com.example.scantoserve.repository.RestaurantRepo;
import com.example.scantoserve.repository.UserRepo;
import com.example.scantoserve.service.InvoiceService;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {

    private final CustomerOrderRepo orderRepo;
    private final UserRepo userRepo;
    private final RestaurantRepo restaurantRepo;

    @Override
    public byte[] generateInvoice(Long orderId) {

        User currentUser = getCurrentUser();

        CustomerOrder order = orderRepo.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        validateOrderAccess(order, currentUser);

        try (ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4);

            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 20
            );

            Font headingFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 12
            );

            Font normalFont = FontFactory.getFont(
                    FontFactory.HELVETICA, 10
            );

            // Invoice title
            Paragraph title = new Paragraph(
                    "SCAN TO SERVE - INVOICE",
                    titleFont
            );

            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Restaurant details
            document.add(new Paragraph(
                    order.getRestaurant().getName(),
                    headingFont
            ));

            document.add(new Paragraph(
                    formatAddress(order.getRestaurant()),
                    normalFont
            ));
            document.add(new Paragraph(
                    "Email: " + order.getRestaurant().getEmail(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Phone: " + order.getRestaurant().getMobileNumber(),
                    normalFont
            ));

            document.add(new Paragraph(" "));

            // Order details
            document.add(new Paragraph(
                    "Order ID: " + order.getId(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Table Number: " +
                            order.getDiningTable().getTableNumber(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Customer: " + order.getCustomer().getFullName(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Customer Email: " + order.getCustomer().getEmail(),
                    normalFont
            ));

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

            document.add(new Paragraph(
                    "Order Date: " +
                            order.getCreatedAt().format(formatter),
                    normalFont
            ));

            document.add(new Paragraph(" "));

            // Ordered items table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1, 2, 2, 2});

            addHeader(table, "Item");
            addHeader(table, "Qty");
            addHeader(table, "Unit Price");
            addHeader(table, "Subtotal");
            addHeader(table, "Total");

            for (OrderItem item : order.getItems()) {

                addCell(table, item.getItemName());
                addCell(table, String.valueOf(item.getQuantity()));

                addCell(table,
                        formatAmount(item.getUnitPrice()));

                addCell(table,
                        formatAmount(item.getSubtotal()));

                addCell(table,
                        formatAmount(item.getSubtotal()));
            }

            document.add(table);

            document.add(new Paragraph(" "));

            // Bill summary
            document.add(new Paragraph(
                    "Subtotal: Rs. " +
                            formatAmount(order.getSubtotal()),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Tax: Rs. " +
                            formatAmount(order.getTaxAmount()),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Discount: Rs. " +
                            formatAmount(order.getDiscountAmount()),
                    normalFont
            ));

            Paragraph total = new Paragraph(
                    "Grand Total: Rs. " +
                            formatAmount(order.getTotalAmount()),
                    headingFont
            );

            total.setSpacingBefore(10);
            document.add(total);

            document.add(new Paragraph(" "));

            document.add(new Paragraph(
                    "Order Status: " + order.getOrderStatus(),
                    normalFont
            ));

            document.add(new Paragraph(
                    "Payment Status: " + order.getPaymentStatus(),
                    normalFont
            ));

            Paragraph footer = new Paragraph(
                    "Thank you for visiting!",
                    headingFont
            );

            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);

            document.add(footer);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Unable to generate invoice PDF",
                    exception
            );
        }
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserDetails userDetails)) {

            throw new AccessDeniedException("User is not authenticated");
        }

        return userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    private void validateOrderAccess(
            CustomerOrder order,
            User user) {

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return;
        }

        if (user.getRole() == UserRole.CUSTOMER &&
                order.getCustomer().getId().equals(user.getId())) {
            return;
        }

        if (user.getRole() == UserRole.RESTAURANT_ADMIN &&
                restaurantRepo.findByIdAndOwner_Id(
                        order.getRestaurant().getId(),
                        user.getId()
                ).isPresent()) {
            return;
        }

        throw new AccessDeniedException(
                "You are not authorized to access this invoice"
        );
    }

    private void addHeader(PdfPTable table, String value) {

        PdfPCell cell = new PdfPCell(
                new Phrase(value)
        );

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);

        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String value) {

        PdfPCell cell = new PdfPCell(
                new Phrase(value == null ? "" : value)
        );

        cell.setPadding(6);

        table.addCell(cell);
    }

    private String formatAmount(BigDecimal amount) {

        return amount == null
                ? "0.00"
                : amount.setScale(2, java.math.RoundingMode.HALF_UP)
                .toPlainString();
    }

    private String formatAddress(Restaurant restaurant) {

        return String.join(", ",
                Stream.of(
                                restaurant.getAddressLine(),
                                restaurant.getCity(),
                                restaurant.getState(),
                                restaurant.getPostalCode(),
                                restaurant.getCountry()
                        )
                        .filter(value -> value != null && !value.isBlank())
                        .toList()
        );
    }



}
