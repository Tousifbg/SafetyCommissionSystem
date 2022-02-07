package com.example.publicsafetycomission.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.example.publicsafetycomission.R;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.onurkagan.ksnack_lib.Animations.Fade;
import com.onurkagan.ksnack_lib.MinimalKSnack.MinimalKSnack;
import com.onurkagan.ksnack_lib.MinimalKSnack.MinimalKSnackStyle;

public class ShowNow {
    MinimalKSnack minimalKSnack;
    Context context;
    private KProgressHUD hud;

    public ShowNow(Context context) {
        this.context = context;
        minimalKSnack=new MinimalKSnack((Activity) context);
    }
    public void desplayErrorToast(Context context, String message){
        minimalKSnack
                .setMessage(message) // message
                .setStyle(MinimalKSnackStyle.STYLE_ERROR) // style
                .setBackgroundColor(R.color.error_color) // background color
                .setAnimation(Fade.In.getAnimation(), Fade.Out.getAnimation()) // show and hide animations
                .setDuration(4000) // you can use for auto close.
                .alignBottom() // bottom align option.
                .show();

    }
    public void desplayPositiveToast(Context context, String message){
        minimalKSnack
                .setMessage(message) // message
                .setStyle(MinimalKSnackStyle.STYLE_ERROR) // style
                .setBackgroundColor(R.color.greenn) // background color
                .setAnimation(Fade.In.getAnimation(), Fade.Out.getAnimation()) // show and hide animations
                .setDuration(4000) // you can use for auto close.
                .alignBottom() // bottom align option.
                .show();

    }

    public void showLoadingDialog(final Context context){
        hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface
                                                           dialogInterface)
                    {
                        Toast.makeText(context, "You " +
                                "cancelled manually!", Toast
                                .LENGTH_SHORT).show();
                    }
                });

        hud.show();

    }

    public void scheduleDismiss() {
        hud.dismiss();

    }
}
