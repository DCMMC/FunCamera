package tk.dcmmcc.funcamera;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.itemanimators.AlphaCrossFadeAnimator;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.stephentuso.welcome.WelcomeHelper;

import tk.dcmmcc.funcamera.tensorflow.DetectorActivity;

/**
 * 主Activity
 */
public class MainActivity extends AppCompatActivity {
    //保存好生成的抽屉
    private AccountHeader headerResult = null;
    private Drawer drawerResult = null;
    //欢迎界面
    private WelcomeHelper welcomeScreen;

    /**
     * onCreate初始化方法
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews(savedInstanceState);
    }

    /**
     * 初始化所有Views
     */
    private void initViews(Bundle savedInstanceState) {
        //欢迎界面
        // The welcome screen for this app (only one that automatically shows)
        welcomeScreen = new WelcomeHelper(this, FunCameraWelcomeActivity.class);
        welcomeScreen.show(savedInstanceState);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.header)
                .withSavedInstance(savedInstanceState)
                .build();

        //Create the drawer
        drawerResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withItemAnimator(new AlphaCrossFadeAnimator())
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home)
                                .withDescription(R.string.drawer_item_homeDesc)
                                .withIcon(GoogleMaterial.Icon.gmd_home)
                                .withIdentifier(1)
                                .withSelectable(true),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_setting)
                                .withDescription(R.string.drawer_item_settingDesc)
                                .withIcon(GoogleMaterial.Icon.gmd_settings)
                                .withIdentifier(2)
                                .withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.about)
                                .withDescription(R.string.drawer_item_aboutDesc)
                                .withIcon(GoogleMaterial.Icon.gmd_info)
                                .withIdentifier(3)
                                .withSelectable(true),
                        new SectionDrawerItem().withName(R.string.drawer_item_section_header),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_open_source)
                                .withIcon(FontAwesome.Icon.faw_github)
                                .withIdentifier(4)
                                .withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_exit)
                                .withIcon(GoogleMaterial.Icon.gmd_exit_to_app)
                                .withIdentifier(5)
                                .withTag("Bullhorn")
                                .withSelectable(false)
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //check if the drawerItem is set.
                        //there are different reasons for the drawerItem to be null
                        //--> click on the header
                        //--> click on the footer
                        //those items don't contain a drawerItem

                        if (drawerItem != null) {
                            Intent intent = null;
                            switch ((int) drawerItem.getIdentifier()) {
                                case 1 : break;
                                case 2 : intent = new Intent(MainActivity.this, SettingsActivity.class);
                                    break;
                                case 3 : intent = new Intent(MainActivity.this, AboutActivity.class);
                                    break;
                                case 4 : intent = new LibsBuilder()
                                        .withFields(R.string.class.getFields())
                                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                        .intent(MainActivity.this); break;
                                case 5 : //退出
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(0); break;
                            }

                            if (intent != null) {
                                MainActivity.this.startActivity(intent);
                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .build();

        //only set the active selection if we do not recreate the activity
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 11
            drawerResult.setSelection(1, false);
        }

        //handle cardViews
        CardView cardCamera = (CardView) findViewById(R.id.card_camera),
                cardObjectDetect = (CardView) findViewById(R.id.card_object_detect);
        cardCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainCameraActivity.class);
                startActivity(intent);
            }
        });
        cardObjectDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetectorActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 在系统将要销毁该Activity之前保存一些必要的数据
     * @param outState 存储信息用的容器
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //add the values which need to be saved from the drawer to the bundle
        outState = drawerResult.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerResult.saveInstanceState(outState);

        // This is the only one needed because it is the only one that
        // is shown automatically. The others are only force shown.
        welcomeScreen.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (drawerResult != null && drawerResult.isDrawerOpen()) {
            drawerResult.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}///~
