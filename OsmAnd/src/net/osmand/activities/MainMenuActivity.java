package net.osmand.activities;

import java.io.File;
import java.text.MessageFormat;

import net.osmand.R;
import net.osmand.Version;
import net.osmand.activities.search.SearchActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainMenuActivity extends Activity {

	private static final String FIRST_TIME_APP_RUN = "FIRST_TIME_APP_RUN"; //$NON-NLS-1$
	private static final String EXCEPTION_FILE_SIZE = "/osmand/exception.log"; //$NON-NLS-1$
	
	private Button showMap;
	private Button settingsButton;
	private Button searchButton;
	private Button favouritesButton;
	
	

	
	public void checkPreviousRunsForExceptions() {
		long size = getPreferences(MODE_WORLD_READABLE).getLong(EXCEPTION_FILE_SIZE, 0);
		final File file = new File(Environment.getExternalStorageDirectory(), OsmandApplication.EXCEPTION_PATH);
		if (file.exists() && file.length() > 0) {
			if (size != file.length()) {
				String msg = MessageFormat.format(getString(R.string.previous_run_crashed), OsmandApplication.EXCEPTION_PATH);
				Builder builder = new AlertDialog.Builder(MainMenuActivity.this);
				builder.setMessage(msg).setNeutralButton(getString(R.string.close), null);
				builder.setPositiveButton(R.string.send_report, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "osmand.app@gmail.com" }); //$NON-NLS-1$
						intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
						intent.setType("vnd.android.cursor.dir/email"); //$NON-NLS-1$
						intent.putExtra(Intent.EXTRA_SUBJECT, "OsmAnd bug"); //$NON-NLS-1$
						StringBuilder text = new StringBuilder();
						text.append("\nDevice : ").append(Build.DEVICE); //$NON-NLS-1$
						text.append("\nBrand : ").append(Build.BRAND); //$NON-NLS-1$
						text.append("\nModel : ").append(Build.MODEL); //$NON-NLS-1$
						text.append("\nProduct : ").append(Build.PRODUCT); //$NON-NLS-1$
						text.append("\nBuild : ").append(Build.DISPLAY); //$NON-NLS-1$
						text.append("\nVersion : ").append(Build.VERSION.RELEASE); //$NON-NLS-1$
						text.append("\nApp Version : ").append(Version.APP_NAME_VERSION); //$NON-NLS-1$
						try {
							PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
							if (info != null) {
								text.append("\nApk Version : ").append(info.versionName).append(" ").append(info.versionCode); //$NON-NLS-1$ //$NON-NLS-2$
							}
						} catch (NameNotFoundException e) {
						}
						intent.putExtra(Intent.EXTRA_TEXT, text.toString());
						startActivity(Intent.createChooser(intent, getString(R.string.send_report)));
					}

				});
				builder.show();
				getPreferences(MODE_WORLD_READABLE).edit().putLong(EXCEPTION_FILE_SIZE, file.length()).commit();
			}

		} else {
			if (size > 0) {
				getPreferences(MODE_WORLD_READABLE).edit().putLong(EXCEPTION_FILE_SIZE, 0).commit();
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(Version.VELCOM_EDITION ? R.layout.menu_velcom : R.layout.menu);
		if(Version.VELCOM_EDITION){
			final ImageView imgView = (ImageView) findViewById(R.id.VelcomMini);
			final Camera camera = new Camera();
			final float firstRotate = 0.3f;
			final float invisibleText = 0.7f;
			final int animationTime = 3600;
			Animation ra = new Animation(){
				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					final Matrix matrix = t.getMatrix();
					int centerY = imgView.getHeight() / 2;
					int centerX = imgView.getWidth() / 2;
					camera.save();
					if (interpolatedTime < firstRotate) {
						camera.rotateY(360 * (firstRotate - interpolatedTime) / firstRotate);
					} else if (interpolatedTime < 2 * firstRotate) {
						camera.rotateY(360 * (2 * firstRotate - interpolatedTime) / firstRotate);
					} else {
						camera.rotateY(360 * (interpolatedTime - 2 * firstRotate) / (1 - 2 * firstRotate));
					}
					
					camera.getMatrix(matrix);
					matrix.preTranslate(-centerX, -centerY);
					matrix.postTranslate(centerX, centerY);
					camera.restore();
				}
			};
			ra.setDuration(animationTime);
			imgView.startAnimation(ra);
			  
			final TextView textView = (TextView) findViewById(R.id.TextVelcom);
			Animation alphaAnimation = new Animation(){
				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					if(interpolatedTime < invisibleText){
						t.setAlpha(0);
					} else {
						t.setAlpha((interpolatedTime - invisibleText) / (1 - invisibleText));
					}
				}
				
			};
			alphaAnimation.setAnimationListener(new Animation.AnimationListener(){

				@Override
				public void onAnimationEnd(Animation animation) {
					textView.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationStart(Animation animation) {
					textView.setVisibility(View.VISIBLE);
				}
				
			});
			alphaAnimation.setDuration(animationTime);
			textView.startAnimation(alphaAnimation);
			textView.setVisibility(View.INVISIBLE);
		}

		showMap = (Button) findViewById(R.id.MapButton);
		showMap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent mapIndent = new Intent(MainMenuActivity.this, MapActivity.class);
				startActivityForResult(mapIndent, 0);
			}
		});
		settingsButton = (Button) findViewById(R.id.SettingsButton);
		settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent settings = new Intent(MainMenuActivity.this, SettingsActivity.class);
				startActivity(settings);
			}
		});
		
		favouritesButton = (Button) findViewById(R.id.FavoritesButton);
		favouritesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent settings = new Intent(MainMenuActivity.this, FavouritesActivity.class);
				startActivity(settings);
			}
		});
		
		searchButton = (Button) findViewById(R.id.SearchButton);
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent search = new Intent(MainMenuActivity.this, SearchActivity.class);
				startActivity(search);
			}
		});
		
		
		((OsmandApplication)getApplication()).checkApplicationIsBeingInitialized(this);
		checkPreviousRunsForExceptions();
		
		SharedPreferences pref = getPreferences(MODE_WORLD_WRITEABLE);
		if(!pref.contains(FIRST_TIME_APP_RUN)){
			pref.edit().putBoolean(FIRST_TIME_APP_RUN, true).commit();
			Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.first_time_msg);
			builder.setPositiveButton(R.string.first_time_download, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(MainMenuActivity.this, DownloadIndexActivity.class));
				}
				
			});
			builder.setNegativeButton(R.string.first_time_continue, null);
			
			builder.show();
		}
	}
	

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH
                && event.getRepeatCount() == 0) {
			final Intent search = new Intent(MainMenuActivity.this, SearchActivity.class);
			startActivity(search);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
	
	
}
