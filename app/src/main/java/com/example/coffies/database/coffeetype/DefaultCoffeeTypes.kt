package com.example.coffies.database.coffeetype

import com.example.coffies.database.AppDatabase

object DefaultCoffeeTypes {
    val list = listOf(
        CoffeeType(name = "Эспрессо S", default_volume_ml = 30, default_caffeine_mg_ml = 2.50f),
        CoffeeType(name = "Эспрессо M", default_volume_ml = 40, default_caffeine_mg_ml = 2.50f),
        CoffeeType(name = "Эспрессо L", default_volume_ml = 60, default_caffeine_mg_ml = 2.30f),
        CoffeeType(name = "Ристретто", default_volume_ml = 25, default_caffeine_mg_ml = 2.80f),
        CoffeeType(name = "Лунго", default_volume_ml = 80, default_caffeine_mg_ml = 1.50f),
        CoffeeType(name = "Американо S", default_volume_ml = 150, default_caffeine_mg_ml = 0.65f),
        CoffeeType(name = "Американо M", default_volume_ml = 200, default_caffeine_mg_ml = 0.55f),
        CoffeeType(name = "Американо L", default_volume_ml = 300, default_caffeine_mg_ml = 0.45f),
        CoffeeType(name = "Фильтр S", default_volume_ml = 150, default_caffeine_mg_ml = 0.42f),
        CoffeeType(name = "Фильтр M", default_volume_ml = 250, default_caffeine_mg_ml = 0.40f),
        CoffeeType(name = "Фильтр L", default_volume_ml = 350, default_caffeine_mg_ml = 0.38f),
        CoffeeType(name = "Френч-пресс S", default_volume_ml = 150, default_caffeine_mg_ml = 0.55f),
        CoffeeType(name = "Френч-пресс M", default_volume_ml = 250, default_caffeine_mg_ml = 0.50f),
        CoffeeType(name = "Френч-пресс L", default_volume_ml = 350, default_caffeine_mg_ml = 0.45f),
        CoffeeType(name = "Аэропресс", default_volume_ml = 150, default_caffeine_mg_ml = 0.85f),
        CoffeeType(name = "Капучино S", default_volume_ml = 150, default_caffeine_mg_ml = 0.35f),
        CoffeeType(name = "Капучино M", default_volume_ml = 200, default_caffeine_mg_ml = 0.32f),
        CoffeeType(name = "Капучино L", default_volume_ml = 300, default_caffeine_mg_ml = 0.30f),
        CoffeeType(name = "Латте S", default_volume_ml = 200, default_caffeine_mg_ml = 0.34f),
        CoffeeType(name = "Латте M", default_volume_ml = 300, default_caffeine_mg_ml = 0.31f),
        CoffeeType(name = "Латте L", default_volume_ml = 400, default_caffeine_mg_ml = 0.28f),
        CoffeeType(name = "Флэт уайт", default_volume_ml = 150, default_caffeine_mg_ml = 0.91f),
        CoffeeType(name = "Макиато", default_volume_ml = 50, default_caffeine_mg_ml = 1.30f),
        CoffeeType(name = "Турецкий S", default_volume_ml = 80, default_caffeine_mg_ml = 1.20f),
        CoffeeType(name = "Турецкий M", default_volume_ml = 120, default_caffeine_mg_ml = 1.00f),
        CoffeeType(name = "Вьетнамский", default_volume_ml = 100, default_caffeine_mg_ml = 1.30f),
        CoffeeType(name = "Растворимый S", default_volume_ml = 150, default_caffeine_mg_ml = 0.36f),
        CoffeeType(name = "Растворимый M", default_volume_ml = 250, default_caffeine_mg_ml = 0.28f),
        CoffeeType(name = "Декаф S", default_volume_ml = 150, default_caffeine_mg_ml = 0.04f),
        CoffeeType(name = "Декаф M", default_volume_ml = 250, default_caffeine_mg_ml = 0.03f)
    )
}

suspend fun insertDefaultCoffeeTypesIfNeeded(db: AppDatabase) {
    val coffeeTypeDao = db.coffeeTypeDao()
    if (coffeeTypeDao.getCount() == 0) {
        coffeeTypeDao.insertAll(DefaultCoffeeTypes.list)
    }
}