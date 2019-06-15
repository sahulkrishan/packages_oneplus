package com.oneplus.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import com.oneplus.lib.widget.OPEditText;
import com.oneplus.settings.ui.ColorPickerView;
import com.oneplus.settings.ui.ColorPickerView.OnColorChangedListener;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.regex.Pattern;

public class OPColorPickerActivity extends BaseActivity implements OnClickListener, OnColorChangedListener {
    private static final int SAVE_NEMU = 0;
    private static final String TAG = "OPFullScreenGestureGuidePage";
    private View mColor;
    private EditText mColorEditView;
    private EditText mColorText;
    private String mCurrentColor;
    private String mCurrentTempColor;
    private AlertDialog mEditColorDialog;
    private ColorPickerView mPickerView;
    private ImageView mPreviewAutoBrightness;
    private SeekBar mPreviewSeekBar;
    private Switch mPreviewSwitch;
    private TextView mPreviewText;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_color_picker_layout);
        this.mPreviewText = (TextView) findViewById(R.id.oneplus_custom_color_preview);
        this.mPreviewSwitch = (Switch) findViewById(R.id.oneplus_custom_color_preview_switch);
        this.mPreviewSwitch.setChecked(true);
        this.mPreviewSeekBar = (SeekBar) findViewById(R.id.oneplus_custom_color_preview_seekbar);
        this.mPreviewSeekBar.setProgress(50);
        this.mPreviewSeekBar.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        this.mPreviewAutoBrightness = (ImageView) findViewById(R.id.oneplus_custom_color_preview_brightness);
        this.mColorText = (EditText) findViewById(R.id.oneplus_color_edit_view);
        this.mColorText.requestFocus();
        this.mColorText.setOnClickListener(this);
        this.mPickerView = (ColorPickerView) findViewById(R.id.oneplus_color_picker_view);
        this.mPickerView.setOnColorChangedListener(this);
        Intent intent = getIntent();
        if (intent != null) {
            this.mCurrentColor = intent.getStringExtra("current_color");
            this.mCurrentTempColor = intent.getStringExtra("current_color");
            if (TextUtils.isEmpty(this.mCurrentColor)) {
                this.mCurrentColor = OPConstants.ONEPLUS_ACCENT_RED_COLOR;
                this.mCurrentTempColor = OPConstants.ONEPLUS_ACCENT_RED_COLOR;
                this.mPickerView.setColor(OPUtils.parseColor(OPConstants.ONEPLUS_ACCENT_RED_COLOR));
            } else {
                this.mPickerView.setColor(OPUtils.parseColor(this.mCurrentColor));
            }
            this.mColorText.setText(this.mCurrentColor);
            refreshUI(OPUtils.parseColor(this.mCurrentColor), this.mCurrentColor);
        }
    }

    private void refreshUI(int color, String colorStr) {
        this.mPreviewText.setTextColor(OPUtils.parseColor(colorStr));
        this.mPreviewSwitch.getTrackDrawable().setTintList(ColorStateList.valueOf(OPUtils.parseColor(colorStr)));
        this.mPreviewSwitch.getThumbDrawable().setTintList(ColorStateList.valueOf(OPUtils.parseColor(colorStr)));
        this.mPreviewSeekBar.getThumb().setTintList(ColorStateList.valueOf(OPUtils.parseColor(colorStr)));
        this.mPreviewSeekBar.getBackground().setTintList(ColorStateList.valueOf(OPUtils.parseColor(colorStr)));
        this.mPreviewSeekBar.getProgressDrawable().setTintList(ColorStateList.valueOf(OPUtils.parseColor(colorStr)));
        this.mPreviewAutoBrightness.setImageTintList(ColorStateList.valueOf(OPUtils.parseColor(colorStr)));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        addOptionsMenuItems(menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Access modifiers changed, original: 0000 */
    public void addOptionsMenuItems(Menu menu) {
        menu.add(0, 0, 0, R.string.wifi_menu_configure).setIcon(R.drawable.op_ic_check_black_24px).setShowAsAction(1);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == 0) {
            Intent intent = new Intent();
            intent.putExtra("current_temp_color", this.mCurrentColor);
            setResult(-1, intent);
            finish();
            return true;
        } else if (itemId != 16908332) {
            return super.onOptionsItemSelected(item);
        } else {
            onBackPressed();
            return true;
        }
    }

    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("current_temp_color", this.mCurrentTempColor);
        setResult(-1, intent);
        finish();
    }

    public void onColorChanged(int color) {
        StringBuffer argb = new StringBuffer("#");
        argb.append(Integer.toHexString(Color.red(color)));
        argb.append(Integer.toHexString(Color.green(color)));
        argb.append(Integer.toHexString(Color.blue(color)));
        EditText editText = this.mColorText;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("#");
        stringBuilder.append(convertToRGB(color));
        editText.setText(stringBuilder.toString());
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("#");
        stringBuilder2.append(convertToRGB(color));
        this.mCurrentColor = stringBuilder2.toString();
        refreshUI(color, this.mCurrentColor);
    }

    private String convertToRGB(int color) {
        StringBuilder stringBuilder;
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));
        if (red.length() == 1) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("0");
            stringBuilder.append(red);
            red = stringBuilder.toString();
        }
        if (green.length() == 1) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("0");
            stringBuilder.append(green);
            green = stringBuilder.toString();
        }
        if (blue.length() == 1) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("0");
            stringBuilder.append(blue);
            blue = stringBuilder.toString();
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append(red);
        stringBuilder.append(green);
        stringBuilder.append(blue);
        return stringBuilder.toString();
    }

    public static int convertToColorInt(String argb) throws IllegalArgumentException {
        if (!argb.startsWith("#")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("#");
            stringBuilder.append(argb);
            argb = stringBuilder.toString();
        }
        return OPUtils.parseColor(argb);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.oneplus_color_edit_view) {
            showColotEditDialog();
        }
    }

    private boolean isColorCodeValid(String color) {
        return Pattern.compile("^#([0-9a-fA-F]{6})").matcher(color).matches();
    }

    private void setEditTextAtLastLocation(EditText editText) {
        CharSequence text = editText.getText();
        if (text instanceof Spannable) {
            Selection.setSelection((Spannable) text, text.length());
        }
    }

    public void showColotEditDialog() {
        View editView = LayoutInflater.from(this).inflate(R.layout.op_fingerprint_rename_dialog, null);
        this.mColorEditView = (OPEditText) editView.findViewById(R.id.opfinger_rename_ed);
        this.mColorEditView.requestFocus();
        this.mColorEditView.setText(this.mCurrentColor);
        if (!TextUtils.isEmpty(this.mCurrentColor)) {
            this.mColorEditView.setSelection(this.mCurrentColor.length());
        }
        this.mColorEditView.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Editable mEditable = OPColorPickerActivity.this.mColorEditView.getText();
                if (mEditable != null && mEditable.length() == 0) {
                    OPColorPickerActivity.this.mColorEditView.setText("#");
                    OPColorPickerActivity.this.setEditTextAtLastLocation(OPColorPickerActivity.this.mColorEditView);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        this.mEditColorDialog = new Builder(this).setTitle(R.string.op_custom_color_value_edit).setView(editView).setCancelable(true).setPositiveButton(R.string.okay, null).setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();
        this.mEditColorDialog.show();
        this.mEditColorDialog.getButton(-1).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String colorText = OPColorPickerActivity.this.mColorEditView.getText().toString();
                if (OPColorPickerActivity.this.isColorCodeValid(colorText)) {
                    OPColorPickerActivity.this.mCurrentColor = colorText;
                    OPColorPickerActivity.this.mPickerView.setColor(OPColorPickerActivity.convertToColorInt(colorText));
                    OPColorPickerActivity.this.mColorText.setText(colorText);
                    OPColorPickerActivity.this.refreshUI(0, colorText);
                    OPColorPickerActivity.this.mEditColorDialog.dismiss();
                    return;
                }
                Toast.makeText(OPColorPickerActivity.this, R.string.op_custom_color_value_invalid, 0).show();
            }
        });
    }
}
