package ru.azor.core.order_test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.validation.BindingResult;
import ru.azor.api.carts.CartDto;
import ru.azor.api.carts.CartItemDto;
import ru.azor.api.core.OrderDetailsDto;
import ru.azor.api.enums.OrderStatus;
import ru.azor.core.entities.Category;
import ru.azor.core.entities.Order;
import ru.azor.core.entities.Product;
import ru.azor.core.integrations.CartServiceIntegration;
import ru.azor.core.repositories.OrdersRepository;
import ru.azor.core.services.OrdersService;
import ru.azor.core.services.OrderStatisticService;
import ru.azor.core.services.ProductsService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@SpringBootTest(classes = {OrdersService.class})
public class OrdersServiceTest {
    @Autowired
    private OrdersService ordersService;
    @MockBean
    private OrdersRepository ordersRepository;
    @MockBean
    private CartServiceIntegration cartServiceIntegration;
    @MockBean
    private ProductsService productsService;
    @MockBean
    private BindingResult bindingResult;
    @MockBean
    private OrderStatisticService orderStatisticService;
    private final static String USERNAME = "test_username";
    private final static String FULL_NAME = "test_full_name";
    private final static String ADDRESS = "test_address";
    private final static String PHONE = "test_phone";
    private final static String CATEGORY_TITLE = "test_category_title";
    private static OrderDetailsDto orderDetailsDto;
    private static CartItemDto cartItemDto;
    private static CartDto cartDto;
    private static Product product;

    @BeforeAll
    public static void initEntities() {
        orderDetailsDto = new OrderDetailsDto();
        orderDetailsDto.setFullName(FULL_NAME);
        orderDetailsDto.setAddress(ADDRESS);
        orderDetailsDto.setPhone(PHONE);
        cartItemDto = new CartItemDto();
        cartItemDto.setProductId(1L);
        cartItemDto.setProductTitle("Milk");
        cartItemDto.setPrice(BigDecimal.valueOf(100));
        cartItemDto.setQuantity(1);
        cartItemDto.setPricePerProduct(BigDecimal.valueOf(100));
        cartDto = new CartDto();
        cartDto.setTotalPrice(BigDecimal.valueOf(100));
        cartDto.setItems(List.of(cartItemDto));
        product = new Product();
        product.setId(cartItemDto.getProductId());
        product.setTitle(cartItemDto.getProductTitle());
        product.setPrice(cartItemDto.getPricePerProduct());
        product.setCategories(Set.of(new Category(1L, CATEGORY_TITLE)));
    }

    @Test
    public void createOrderTest() {
        Mockito.doReturn(cartDto).when(cartServiceIntegration).getUserCart(USERNAME);
        Mockito.doReturn(Optional.of(product)).when(productsService).findById(cartItemDto.getProductId());
        Mockito.doNothing().when(orderStatisticService).addStatistic(new CopyOnWriteArraySet<>());
        Order order = ordersService.save(USERNAME, orderDetailsDto, bindingResult);
        Assertions.assertEquals(order.getOrderStatus(), OrderStatus.CREATED);
        Assertions.assertEquals(order.getTotalPrice(), cartDto.getTotalPrice());
        Assertions.assertEquals(order.getItems().size(), cartDto.getItems().size());
        Mockito.verify(cartServiceIntegration, Mockito.times(1)).clearUserCart(USERNAME);
    }
}
