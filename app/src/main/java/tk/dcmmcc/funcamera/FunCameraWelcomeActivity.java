package tk.dcmmcc.funcamera;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.ParallaxPage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

/**
 * 欢迎页面
 */
public class FunCameraWelcomeActivity extends WelcomeActivity {

    /**
     * Override the Activity's configuration() method.
     * Use WelcomeConfiguration.Builder to set it up
     * @return Welcome界面的配置类
     */
    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .defaultTitleTypefacePath("Montserrat-Bold.ttf")
                .defaultHeaderTypefacePath("Montserrat-Bold.ttf")

                .page(new BasicPage(R.drawable.ic_front_desk_white,
                        getResources().getString(R.string.welcome),
                        getResources().getString(R.string.welcome_detail))
                        .background(R.color.orange_background)
                )

                .page(new BasicPage(R.drawable.ic_thumb_up_white,
                        getResources().getString(R.string.easy_to_use),
                        getResources().getString(R.string.easy_to_use_detail))
                        .background(R.color.red_background)
                )

                .page(new ParallaxPage(R.layout.parallax_ai,
                        getResources().getString(R.string.ai_power),
                        getResources().getString(R.string.ai_power_detail))
                        .lastParallaxFactor(2f)
                        .background(R.color.purple_background)
                )

                .page(new BasicPage(R.drawable.ic_edit_white,
                        getResources().getString(R.string.customizable),
                        getResources().getString(R.string.customizable_detail))
                        .background(R.color.blue_background)
                )

                .swipeToDismiss(true)
                .exitAnimation(android.R.anim.fade_out)
                .build();
    }

    /**
     * If you want to use multiple welcome screens (in different parts of your app)
     * or have updated one and want to show it again, you can assign keys (Make sure
     * they are unique!) to welcome screens by adding the following to your welcome
     * screen Activity.
     * @return Unique String
     */
    public static String welcomeKey() {
        return "Main Welcome Unique Screen";
    }
}
