package com.example.loginpage.data

import com.example.loginpage.R
import com.example.loginpage.model.Banner
import com.example.loginpage.model.Perfume

class DataSource {

    fun loadMenPerfumes(): List<Perfume> {
        return listOf(
            Perfume(0, R.string.perfume_dior_sauvage, R.drawable.mperfume1, 8990.0, R.string.desc_dior_sauvage),
            Perfume(1, R.string.perfume_creed, R.drawable.mperfume2, 8450.0, R.string.desc_creed),
            Perfume(2, R.string.perfume_spice_bomb, R.drawable.mperfume3, 7990.0, R.string.desc_spice_bomb),
            Perfume(3, R.string.perfume_versace, R.drawable.mperfume4, 7590.0, R.string.desc_versace),
            Perfume(4, R.string.perfume_prada, R.drawable.mperfume5, 7790.0, R.string.desc_prada),
            Perfume(5, R.string.perfume_tom_ford, R.drawable.mperfume6, 8890.0, R.string.desc_tom_ford),
            Perfume(6, R.string.perfume_eternity, R.drawable.mperfume7, 7390.0, R.string.desc_eternity),
            Perfume(7, R.string.perfume_tommy, R.drawable.mperfume8, 7250.0, R.string.desc_tommy),
            Perfume(8, R.string.perfume_gucci, R.drawable.mperfume9, 9150.0, R.string.desc_gucci),
            Perfume(9, R.string.perfume_hugo_boss, R.drawable.mperfume10, 8490.0, R.string.desc_hugo_boss),
            Perfume(10, R.string.perfume_yves, R.drawable.mperfume11, 7990.0, R.string.desc_yves),
            Perfume(11, R.string.perfume_jean_paul, R.drawable.mperfume12, 8590.0, R.string.desc_jean_paul),
            Perfume(12, R.string.perfume_bella_vita, R.drawable.mperfume13, 7290.0, R.string.desc_bella_vita),
            Perfume(13, R.string.perfume_chanel, R.drawable.mperfume14, 8990.0, R.string.desc_chanel),
            Perfume(14, R.string.perfume_azzaro, R.drawable.mperfume15, 7790.0, R.string.desc_azzaro)
        )
    }

    fun loadWomenPerfumes(): List<Perfume> {
        return listOf(
            Perfume(15, R.string.perfume_ysl, R.drawable.wperfume1, 8990.0, R.string.desc_ysl),
            Perfume(16, R.string.perfume_daisy, R.drawable.wperfume2, 8190.0, R.string.desc_daisy),
            Perfume(17, R.string.perfume_chance_chanel, R.drawable.wperfume3, 7650.0, R.string.desc_chance_chanel),
            Perfume(18, R.string.perfume_flower_bomb, R.drawable.wperfume4, 7090.0, R.string.desc_flower_bomb),
            Perfume(19, R.string.perfume_prada_milano, R.drawable.wperfume5, 8450.0, R.string.desc_prada_milano),
            Perfume(20, R.string.perfume_poison_dior, R.drawable.wperfume6, 7890.0, R.string.desc_poison_dior),
            Perfume(21, R.string.perfume_euphoria, R.drawable.wperfume16, 7750.0, R.string.desc_euphoria),
            Perfume(22, R.string.perfume_billie_eilish, R.drawable.wperfume8, 7590.0, R.string.desc_billie_eilish),
            Perfume(23, R.string.perfume_black_orchid, R.drawable.wperfume9, 8190.0, R.string.desc_black_orchid),
            Perfume(24, R.string.perfume_phlur, R.drawable.wperfume10, 6990.0, R.string.desc_phlur),
            Perfume(25, R.string.perfume_burberry, R.drawable.wperfume11, 7750.0, R.string.desc_burberry),
            Perfume(26, R.string.perfume_adore, R.drawable.wperfume12, 7290.0, R.string.desc_adore),
            Perfume(27, R.string.perfume_byredo, R.drawable.wperfume13, 8150.0, R.string.desc_byredo),
            Perfume(28, R.string.perfume_gucci_flora, R.drawable.wperfume14, 8790.0, R.string.desc_gucci_flora),
            Perfume(29, R.string.perfume_good_girl, R.drawable.wperfume15, 8450.0, R.string.desc_good_girl)
        )
    }

    fun loadNewArrivals(): List<Perfume> {
        return listOf(
            Perfume(19, R.string.perfume_prada_milano, R.drawable.wperfume5, 8450.0, R.string.desc_prada_milano),
            Perfume(20, R.string.perfume_poison_dior, R.drawable.wperfume6, 7890.0, R.string.desc_poison_dior),
            Perfume(21, R.string.perfume_euphoria, R.drawable.wperfume16, 7750.0, R.string.desc_euphoria),
            Perfume(22, R.string.perfume_billie_eilish, R.drawable.wperfume8, 7590.0, R.string.desc_billie_eilish),
            Perfume(3, R.string.perfume_versace, R.drawable.mperfume4, 7590.0, R.string.desc_versace)
        )
    }

    fun loadDiscountedPerfumes(): List<Perfume> {
        return listOf(
            Perfume(
                23,
                R.string.perfume_black_orchid,
                R.drawable.wperfume9,
                7090.0,
                R.string.desc_black_orchid,
                originalPrice = 8190.0
            ),
            Perfume(
                24,
                R.string.perfume_phlur,
                R.drawable.wperfume10,
                6890.0,
                R.string.desc_phlur,
                originalPrice = 7590.0
            ),
            Perfume(
                25,
                R.string.perfume_burberry,
                R.drawable.wperfume11,
                6590.0,
                R.string.desc_burberry,
                originalPrice = 7750.0
            ),
            Perfume(
                26,
                R.string.perfume_adore,
                R.drawable.wperfume12,
                6390.0,
                R.string.desc_adore,
                originalPrice = 7290.0
            ),
            Perfume(
                7,
                R.string.perfume_tommy,
                R.drawable.mperfume8,
                7250.0,
                R.string.desc_tommy,
                originalPrice = 7950.0
            )
        )
    }


    fun loadBanners(): List<Banner> {
        return listOf(
            Banner(R.string.banner_title_1, R.string.banner_subtitle_1, R.drawable.mperfume1, 0),
            Banner(R.string.banner_title_2, R.string.banner_subtitle_2, R.drawable.wperfume1, 15),
            Banner(R.string.banner_title_3, R.string.banner_subtitle_3, R.drawable.mperfume2, 1)
        )
    }


    // ✅ Correct perfume lookup using the ID field
    fun getPerfumeById(id: Int): Perfume? {
        val all = loadMenPerfumes() + loadWomenPerfumes()
        return all.find { it.id == id }
    }
}
