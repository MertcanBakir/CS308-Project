package cs308.backhend.controller;

import cs308.backhend.service.RealWishlistService;
import cs308.backhend.model.Product;
import cs308.backhend.model.RealWishlist;
import cs308.backhend.model.User;
import cs308.backhend.repository.ProductRepo;
import cs308.backhend.repository.RealWishlistRepo;
import cs308.backhend.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealWishlistServiceTest {

    @Mock
    private RealWishlistRepo realWishlistRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private ProductRepo productRepo;

    @InjectMocks
    private RealWishlistService realWishlistService;

    private User user;
    private Product product;
    private RealWishlist wishlistItem;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(2L);

        wishlistItem = new RealWishlist();
        wishlistItem.setId(10L);
        wishlistItem.setUser(user);
        wishlistItem.setProduct(product);
    }

    @Test
    @DisplayName("addToRealWishlist başarılı şekilde yeni öğe eklemeli")
    void testAddToRealWishlistSuccess() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(productRepo.findById(2L)).thenReturn(Optional.of(product));
        when(realWishlistRepo.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
        when(realWishlistRepo.save(any(RealWishlist.class))).thenReturn(wishlistItem);

        RealWishlist result = realWishlistService.addToRealWishlist(1L, 2L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(realWishlistRepo).save(argThat(item ->
                item.getUser().equals(user) && item.getProduct().equals(product)
        ));
    }

    @Test
    @DisplayName("addToRealWishlist kopya eklemeye çalışınca hata fırlatmalı")
    void testAddToRealWishlistDuplicate() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(productRepo.findById(2L)).thenReturn(Optional.of(product));
        when(realWishlistRepo.findByUserAndProduct(user, product)).thenReturn(Optional.of(wishlistItem));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                realWishlistService.addToRealWishlist(1L, 2L)
        );
        assertEquals("Product already in real wishlist", ex.getMessage());
    }

    @Test
    @DisplayName("getRealWishlist kullanıcının listesini döndürmeli")
    void testGetRealWishlist() {
        List<RealWishlist> sampleList = Arrays.asList(wishlistItem);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(realWishlistRepo.findByUser(user)).thenReturn(sampleList);

        List<RealWishlist> result = realWishlistService.getRealWishlist(1L);

        assertEquals(1, result.size());
        assertSame(wishlistItem, result.get(0));
    }

    @Test
    @DisplayName("removeFromRealWishlist başarılı şekilde öğeyi silmeli")
    void testRemoveFromRealWishlistSuccess() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(productRepo.findById(2L)).thenReturn(Optional.of(product));
        when(realWishlistRepo.findByUserAndProduct(user, product)).thenReturn(Optional.of(wishlistItem));

        // exception fırlatılmadan çalışmalı
        assertDoesNotThrow(() -> realWishlistService.removeFromRealWishlist(1L, 2L));
        verify(realWishlistRepo).delete(wishlistItem);
    }

    @Test
    @DisplayName("removeFromRealWishlist bulunamayan öğede hata fırlatmalı")
    void testRemoveFromRealWishlistNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(productRepo.findById(2L)).thenReturn(Optional.of(product));
        when(realWishlistRepo.findByUserAndProduct(user, product)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                realWishlistService.removeFromRealWishlist(1L, 2L)
        );
        assertEquals("Product not found in wishlist", ex.getMessage());
    }
}
