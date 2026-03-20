package com.circledev.notes;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import com.circledev.notes.databinding.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class MainActivity extends Activity {
	
	private MainBinding binding;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = MainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		
		binding.webview1.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView _param1, String _param2, Bitmap _param3) {
				final String _url = _param2;
				
				super.onPageStarted(_param1, _param2, _param3);
			}
			
			@Override
			public void onPageFinished(WebView _param1, String _param2) {
				final String _url = _param2;
				
				super.onPageFinished(_param1, _param2);
			}
		});
	}
	
	private void initializeLogic() {
		binding.webview1.loadUrl("file:///android_asset/assets/index.html");
		binding.webview1.getSettings().setJavaScriptEnabled(true);
		binding.webview1.getSettings().setDomStorageEnabled(true);
		binding.webview1.getSettings().setAllowFileAccess(true);
		binding.webview1.getSettings().setAllowContentAccess(true);
		binding.webview1.getSettings().setAllowFileAccessFromFileURLs(true);
		binding.webview1.getSettings().setSupportZoom(true);
		binding.webview1.getSettings().setBuiltInZoomControls(true);
		binding.webview1.setFocusable(true);
		binding.webview1.setFocusableInTouchMode(true);
		
		// Optional: Fix link handling
		binding.webview1.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		binding.webview1.addJavascriptInterface(new Object() {
			
			@android.webkit.JavascriptInterface
			public void saveData(String json) {
				try {
					java.io.File dir = new java.io.File(getFilesDir(), "noteflow");
					if (!dir.exists()) dir.mkdirs();
					java.io.File file = new java.io.File(dir, "notes.json");
					java.io.FileWriter fw = new java.io.FileWriter(file);
					fw.write(json);
					fw.close();
				} catch (Exception e) { e.printStackTrace(); }
			}
			
			@android.webkit.JavascriptInterface
			public String loadData() {
				try {
					java.io.File file = new java.io.File(getFilesDir() + "/noteflow/notes.json");
					if (!file.exists()) return "";
					java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) sb.append(line);
					br.close();
					return sb.toString();
				} catch (Exception e) { return ""; }
			}
			
		}, "NativeStorage");
		binding.webview1.getSettings().setJavaScriptEnabled(true);
		binding.webview1.getSettings().setDomStorageEnabled(true);
		binding.webview1.getSettings().setAllowFileAccess(true);
		binding.webview1.getSettings().setAllowContentAccess(true);
		
		binding.webview1.addJavascriptInterface(new Object() {
			
			// SIMPAN data ke file internal
			@android.webkit.JavascriptInterface
			public void saveData(String json) {
				try {
					java.io.File dir = new java.io.File(getFilesDir(), "noteflow");
					if (!dir.exists()) dir.mkdirs();
					java.io.File file = new java.io.File(dir, "data.json");
					java.io.FileWriter fw = new java.io.FileWriter(file);
					fw.write(json);
					fw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			// LOAD data dari file internal
			@android.webkit.JavascriptInterface
			public String loadData() {
				try {
					java.io.File file = new java.io.File(
					getFilesDir() + "/noteflow/data.json"
					);
					if (!file.exists()) return "";
					java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.FileReader(file)
					);
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) sb.append(line);
					br.close();
					return sb.toString();
				} catch (Exception e) {
					return "";
				}
			}
			
			// BACKUP ke SDCard
			@android.webkit.JavascriptInterface
			public void backupSD(String json) {
				try {
					java.io.File dir = new java.io.File(
					android.os.Environment.getExternalStorageDirectory(),
					"NoteFlow"
					);
					if (!dir.exists()) dir.mkdirs();
					java.io.FileWriter fw = new java.io.FileWriter(
					new java.io.File(dir, "backup.json")
					);
					fw.write(json);
					fw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			// LOAD backup dari SDCard
			@android.webkit.JavascriptInterface
			public String loadBackupSD() {
				try {
					java.io.File file = new java.io.File(
					android.os.Environment.getExternalStorageDirectory()
					+ "/NoteFlow/backup.json"
					);
					if (!file.exists()) return "";
					java.io.BufferedReader br = new java.io.BufferedReader(
					new java.io.FileReader(file)
					);
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) sb.append(line);
					br.close();
					return sb.toString();
				} catch (Exception e) {
					return "";
				}
			}
			
			// UPDATE WIDGET
			@android.webkit.JavascriptInterface
			public void saveToWidget(String title, String content) {
				try {
					getSharedPreferences("NoteFlowWidget", MODE_PRIVATE)
					.edit()
					.putString("widget_title", title)
					.putString("widget_content", content)
					.apply();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}, "NativeStorage");
		
		// Update widget saat app dibuka
		try {
			android.appwidget.AppWidgetManager awm =
			android.appwidget.AppWidgetManager.getInstance(this);
			
			android.content.ComponentName cn = new android.content.ComponentName(
			this,
			"com.circledev.notes.NoteWidgetProvider"  // pakai String, bukan .class
			);
			
			int[] ids = awm.getAppWidgetIds(cn);
			for (int id : ids) {
				NoteWidgetProvider.updateWidget(this, awm, id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}