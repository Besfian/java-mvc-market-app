package ru.yandex.practicum.mymarket.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ItemRepository itemRepository;

    public DataInitializer(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (itemRepository.count() == 0) {
            initItems();
        }
    }

    private void initItems() {
        String[] titles = {
                "Мяч футбольный", "Теннисная ракетка", "Баскетбольное кольцо",
                "Велосипед горный", "Роликовые коньки", "Самокат",
                "Лыжи горные", "Сноуборд", "Коньки хоккейные",
                "Гантели 5кг", "Штанга", "Скакалка",
                "Батут", "Настольный теннис", "Бадминтон"
        };

        String[] descriptions = {
                "Профессиональный футбольный мяч", "Легкая и прочная ракетка",
                "Регулируемое баскетбольное кольцо", "Горный велосипед 24 скорости",
                "Роликовые коньки для фигурного катания", "Легкий складной самокат",
                "Горные лыжи для профессионалов", "Сноуборд для фристайла",
                "Хоккейные коньки с защитой", "Гантели с неопреновым покрытием",
                "Штанга с блинами 20кг", "Скакалка с регулировкой длины",
                "Детский батут с сеткой", "Набор для настольного тенниса",
                "Ракетки и волан для бадминтона"
        };

        String[] imgPaths = {
                "/images/ball.jpg", "/images/racket.jpg", "/images/hoop.jpg",
                "/images/bike.jpg", "/images/skates.jpg", "/images/scooter.jpg",
                "/images/skis.jpg", "/images/snowboard.jpg", "/images/iceskates.jpg",
                "/images/dumbbell.jpg", "/images/barbell.jpg", "/images/jumprope.jpg",
                "/images/trampoline.jpg", "/images/tabletennis.jpg", "/images/badminton.jpg"
        };

        long[] prices = {2500, 4500, 3500, 35000, 5000, 4000, 28000, 25000, 6000, 1200, 8000, 300, 8000, 2000, 1500};

        for (int i = 0; i < titles.length; i++) {
            Item item = Item.builder()
                    .title(titles[i])
                    .description(descriptions[i])
                    .imgPath(imgPaths[i])
                    .price(prices[i])
                    .build();
            itemRepository.save(item);
        }
    }
}
