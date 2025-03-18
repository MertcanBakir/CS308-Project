package cs308.backhend.service;


import cs308.backhend.model.Product;
import cs308.backhend.model.User;
import cs308.backhend.model.Wishlist;
import cs308.backhend.repository.ProductRepo;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.repository.WishListRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    public Wishlist addToWishlist(Long userId, Long productId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));


        if (wishListRepo.findByUserAndProduct(user, product).isPresent()) {
            throw new RuntimeException("Product is already in the wishlist");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        return wishListRepo.save(wishlist);
    }

    public void deleteFromWishlist(Long wishlistId) {
        Wishlist wishlist = wishListRepo.findById(wishlistId)
                .orElseThrow(() -> new RuntimeException("Wishlist item not found"));

        wishListRepo.delete(wishlist);
    }
}