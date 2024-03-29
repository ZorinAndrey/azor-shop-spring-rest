package ru.azor.cart.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.azor.api.core.ProductDto;
import ru.azor.api.exceptions.ClientException;
import ru.azor.cart.integrations.CoreServiceIntegration;
import ru.azor.cart.models.Cart;

import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CoreServiceIntegration coreServiceIntegration;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CartStatisticService cartStatisticService;

    @Value("${utils.cart.prefix}")
    private String cartPrefix;

    public String getCartUuidFromSuffix(String suffix) {
        return cartPrefix + suffix;
    }

    public String generateCartUuid() {
        return UUID.randomUUID().toString();
    }

    public Cart getCurrentCart(String cartKey) {
        try {
            if (Boolean.FALSE.equals(redisTemplate.hasKey(cartKey))) {
                redisTemplate.opsForValue().set(cartKey, new Cart());
            }
            return (Cart) redisTemplate.opsForValue().get(cartKey);
        } catch (Exception e) {
            throw new ClientException("Корзина не найдена", HttpStatus.NOT_FOUND);
        }
    }

    public void addToCart(String cartKey, Long productId) {
        ProductDto productDto = coreServiceIntegration.findById(productId);
        execute(cartKey, c -> c.add(productDto));
        cartStatisticService.addStatistic(productDto);
    }

    public void clearCart(String cartKey) {
        execute(cartKey, Cart::clear);
    }

    public void removeItemFromCart(String cartKey, Long productId) {
        execute(cartKey, c -> c.remove(productId));
    }

    public void decrementItem(String cartKey, Long productId) {
        execute(cartKey, c -> c.decrement(productId));
    }

    public void incrementItem(String cartKey, Long productId) {
        execute(cartKey, c -> c.increment(productId));
    }

    public void merge(String userCartKey, String guestCartKey) {
        Cart guestCart = getCurrentCart(guestCartKey);
        Cart userCart = getCurrentCart(userCartKey);
        userCart.merge(guestCart);
        updateCart(guestCartKey, guestCart);
        updateCart(userCartKey, userCart);
    }

    private void execute(String cartKey, Consumer<Cart> action) {
        Cart cart = getCurrentCart(cartKey);
        action.accept(cart);
        redisTemplate.opsForValue().set(cartKey, cart);
    }

    private void updateCart(String cartKey, Cart cart) {
        redisTemplate.opsForValue().set(cartKey, cart);
    }
}