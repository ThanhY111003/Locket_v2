package com.example.demo;

import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seed dữ liệu mặc định khi khởi động ứng dụng.
 * Danh mục chi tiêu sẽ được tạo nếu chưa tồn tại.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        seedCategories();
    }

    private void seedCategories() {
        List<String[]> defaultCategories = List.of(
                new String[]{"Ăn uống", "Chi phí ăn uống, nhà hàng, cà phê"},
                new String[]{"Mua sắm", "Quần áo, đồ dùng, mua sắm online"},
                new String[]{"Di chuyển", "Xăng, xe buýt, taxi, Grab"},
                new String[]{"Giải trí", "Phim, game, du lịch, sở thích"},
                new String[]{"Hóa đơn", "Điện, nước, internet, thuê nhà"},
                new String[]{"Khác", "Các khoản chi tiêu khác"}
        );

        int created = 0;
        for (String[] cat : defaultCategories) {
            if (categoryRepository.findByName(cat[0]).isEmpty()) {
                categoryRepository.save(Category.builder()
                        .name(cat[0])
                        .description(cat[1])
                        .build());
                created++;
            }
        }

        if (created > 0) {
            log.info("DataInitializer: Created {} default categories", created);
        } else {
            log.info("DataInitializer: All default categories already exist");
        }
    }
}
