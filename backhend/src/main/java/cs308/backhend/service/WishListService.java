package cs308.backhend.service;

import cs308.backhend.model.Product;
import cs308.backhend.model.User;
import cs308.backhend.model.Wishlist;
import cs308.backhend.repository.ProductRepo;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.repository.WishListRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WishListService {

    private final WishListRepo wishListRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;

    @Autowired
    public WishListService(WishListRepo wishListRepo, UserRepo userRepo, ProductRepo productRepo) {
        this.wishListRepo = wishListRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
    }

    @Transactional
    public Wishlist addToWishlist(Long userId, Long productId, Long quantity) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (wishListRepo.findByUserAndProduct(user, product).isPresent()) {
            throw new RuntimeException("Product is already in the wishlist");
        }

        // Ürünün popülerlik sayacını artır
        product.incrementWishlistCount();
        productRepo.save(product);

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlist.setQuantity(quantity);
        return wishListRepo.save(wishlist);
    }

    public Long getQuantity(Long userId, Long productId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<Wishlist> wishlistItem = wishListRepo.findByUserAndProduct(user, product);
        return wishlistItem.map(Wishlist::getQuantity).orElse(1L);
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<Wishlist> wishlistItem = wishListRepo.findByUserAndProduct(user, product);

        if (wishlistItem.isPresent()) {
            product.decrementWishlistCount();
            productRepo.save(product);


            wishListRepo.delete(wishlistItem.get());
        } else {
            throw new RuntimeException("Product is not in the wishlist");
        }
    }

    @Transactional
    public void updateWishlistQuantity(Long userId, Long productId, Long newQuantity) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<Wishlist> wishlistItemOpt = wishListRepo.findByUserAndProduct(user, product);

        if (wishlistItemOpt.isPresent()) {
            Wishlist wishlistItem = wishlistItemOpt.get();
            wishlistItem.setQuantity(newQuantity);
            wishListRepo.save(wishlistItem);
        } else {
            throw new RuntimeException("Product is not in the wishlist");
        }
    }
}