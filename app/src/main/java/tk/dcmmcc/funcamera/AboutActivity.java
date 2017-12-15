package tk.dcmmcc.funcamera;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

/**
 * 关于界面
 * TODO 修改背景图从本地加载
 * TODO 添加查看欢迎页的功能, -> new WelcomeHelper(this, FunCameraWelcomeActivity.class).forceShow()
 * TODO 修改软件介绍
 */
public class AboutActivity extends AppCompatActivity {

    private AccountHeader headerResult;
    private Drawer result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initViews(savedInstanceState);
    }

    /**
     * init
     */
    private void initViews(Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.about));

        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(false)
                .withHeaderBackground(R.drawable.header)
                .withSavedInstance(savedInstanceState)
                .build();

        result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withToolbar(toolbar)
                .withFullscreen(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home).withDescription(R.string.drawer_item_homeDesc).withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(1).withSelectable(true),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_setting).withDescription(R.string.drawer_item_settingDesc).withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(2).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.about).withDescription(R.string.drawer_item_aboutDesc).withIcon(GoogleMaterial.Icon.gmd_info).withIdentifier(3).withSelectable(false),
                        new SectionDrawerItem().withName(R.string.drawer_item_section_header),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIcon(FontAwesome.Icon.faw_github).withIdentifier(4).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_exit).withIcon(GoogleMaterial.Icon.gmd_exit_to_app).withIdentifier(5).withTag("Bullhorn")
                )
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
                                case 1 : intent = new Intent(AboutActivity.this, MainActivity.class);
                                    break;
                                case 2 : intent = new Intent(AboutActivity.this, SettingsActivity.class);
                                    break;
                                case 3 :break;
                                case 4 : intent = new LibsBuilder()
                                        .withFields(R.string.class.getFields())
                                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                        .intent(AboutActivity.this); break;
                                case 5 : //退出
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(0); break;
                            }

                            if (intent != null) {
                                AboutActivity.this.startActivity(intent);
                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        //only set the active selection if we do not recreate the activity
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 11
            result.setSelection(3, false);
        }

        fillFab();
        loadBackdrop();
    }

    /**
     * 背景图
     */
    private void loadBackdrop() {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        Glide.with(this).load("https://unsplash.it/600/300/?random").centerCrop().into(imageView);
    }

    /**
     * 喜欢此应用吗?
     */
    private void fillFab() {
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floating_action_button);
        fab.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_favorite).actionBar().color(Color.WHITE));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
}
