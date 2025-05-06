package cs308.backhend.service;

import cs308.backhend.model.*;
import cs308.backhend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RealWishlistService {

    private final RealWishlistRepo realWishlistRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;

    public RealWishlist addToRealWishlist(Long userId, Long productId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        if (realWishlistRepo.findByUserAndProduct(user, product).isPresent()) {
            throw new RuntimeException("Product already in real wishlist");
        }

        RealWishlist item = new RealWishlist();
        item.setUser(user);
        item.setProduct(product);
        return realWishlistRepo.save(item);
    }

    public List<RealWishlist> getRealWishlist(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return realWishlistRepo.findByUser(user);
    }

    public void removeFromRealWishlist(Long userId, Long productId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        RealWishlist item = realWishlistRepo.findByUserAndProduct(user, product)
                .orElseThrow(() -> new RuntimeException("Product not found in wishlist"));
        realWishlistRepo.delete(item);
    }
}
