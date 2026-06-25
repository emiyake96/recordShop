package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;
import org.yearup.service.ShoppingCartService;
import org.yearup.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("cart")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class ShoppingCartController
{
    private final ShoppingCartService shoppingCartService;
    private final UserService userService;

    public ShoppingCartController(ShoppingCartService shoppingCartService, UserService userService)
    {
        this.shoppingCartService = shoppingCartService;
        this.userService = userService;
    }

    @GetMapping("")
    public ShoppingCart getCart(Principal principal)
    {
        int userId = getUserId(principal);
        return shoppingCartService.getByUserId(userId);
    }

    @PostMapping("products/{productId}")
    public ResponseEntity<ShoppingCart> addToCart(Principal principal, @PathVariable int productId)
    {
        int userId = getUserId(principal);
        ShoppingCart cart = shoppingCartService.addItem(userId, productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    @PutMapping("products/{productId}")
    public ShoppingCart updateCartItem(Principal principal,
                                       @PathVariable int productId,
                                       @RequestBody ShoppingCartItem item)
    {
        int userId = getUserId(principal);
        return shoppingCartService.updateItem(userId, productId, item.getQuantity());
    }

    @DeleteMapping("")
    public ShoppingCart clearCart(Principal principal)
    {
        int userId = getUserId(principal);
        return shoppingCartService.clearCart(userId);
    }

    // helper to resolve the logged-in user's id, throws 401 if not found
    private int getUserId(Principal principal)
    {
        User user = userService.getByUserName(principal.getName());
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return user.getId();
    }
}
