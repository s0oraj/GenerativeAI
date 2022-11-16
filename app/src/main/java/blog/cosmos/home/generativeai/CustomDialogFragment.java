package blog.cosmos.home.generativeai;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;


/**
 *  Source https://stackoverflow.com/questions/27246981/android-floating-activity-with-dismiss-on-swipe
 *  a combination of two answers were used here. Answers from Gaëtan Maisse and Zain
 */
public class CustomDialogFragment extends androidx.fragment.app.DialogFragment implements View.OnTouchListener{

    LinearLayout rootLayout;
    View viewReference;
    float rootLayoutY=0;
    private float oldY = 0;
    private float baseLayoutPosition = 0;
    private float defaultViewHeight = 0;
    private boolean isScrollingUp = false;
    private boolean isScrollingDown = false;



    Activity activity;
    String imageUrl;
    ImageView dialogBackButton;
    ImageView imageView;
    Button urlButton;


    public CustomDialogFragment() {
        // Required empty public constructor
    }




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);


        View view = inflater.inflate(R.layout.custom_dialog_fragment,container,false);


        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
      //  view.setBackgroundResource(R.drawable.rounded_dialog);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onViewCreated(@NonNull View view1, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view1, savedInstanceState);

        init(view1);
        clickListener(view1);

    }



    private void init(View view) {


        activity= getActivity();

         imageView = view.findViewById(R.id.dialog_image_view);
         urlButton= view.findViewById(R.id.urlButton);

        viewReference= view;
        rootLayout = view.findViewById(R.id.linearDialogLayout);
        dialogBackButton = view.findViewById(R.id.dialogBackBtn);

        if(getArguments() == null){
            return;
        } else {

            imageUrl = getArguments().getString("url");
            Glide.with(getActivity().getApplicationContext())
                    .load(imageUrl)
                    .override(2100,700)
                    .fitCenter()
                    .into(imageView);
        }

    }
    private void clickListener(View view1) {

        rootLayout.setOnTouchListener(this);

        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                defaultViewHeight = rootLayout.getHeight();
            }
        });

        dialogBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        urlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(imageUrl != null){
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(imageUrl));
                    startActivity(i);
                } else{
                    Toast.makeText(getContext(),"Image url is empty",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public boolean onTouch(View view, MotionEvent event) {

        // Get finger position on screen
        final int Y = (int) event.getRawY();

        // Switch on motion event type
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                // save default base layout height
                defaultViewHeight = rootLayout.getHeight();

                // Init finger and view position
                oldY = Y;
                baseLayoutPosition = (int) rootLayout.getY();
                break;

            case MotionEvent.ACTION_UP:


                defaultViewHeight = rootLayout.getHeight();
                if (rootLayoutY >= defaultViewHeight / 2) {
                    dismiss();
                    return true;
                }

                // If user was doing a scroll up
                if(isScrollingUp){
                    // Reset baselayout position
                    rootLayout.setY(0);
                    // We are not in scrolling up mode anymore
                    isScrollingUp = false;
                }

                // If user was doing a scroll down
                if(isScrollingDown){
                    // Reset baselayout position
                    rootLayout.setY(0);
                    // Reset base layout size
                    rootLayout.getLayoutParams().height = (int) defaultViewHeight;
                    rootLayout.requestLayout();
                    // We are not in scrolling down mode anymore
                    isScrollingDown = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:


                rootLayoutY = Math.abs(rootLayout.getY());
                rootLayout.setY( rootLayout.getY() + (Y - oldY));

                if(oldY > Y){
                    if(!isScrollingUp) isScrollingUp = true;
                } else{
                    if(!isScrollingDown) isScrollingDown = true;
                }
                oldY = Y;

                break;
        }
        return true;
    }

    private void setUpKeyboard(){
        //Setup editText behavior for opening soft keyboard
       /* LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);

        userMsgEdt.setOnTouchListener((view, motionEvent) -> {
            InputMethodManager keyboard = (InputMethodManager) getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keyboard != null) {
                isScrollToLastRequired = linearLayoutManager.findLastVisibleItemPosition() == commentAdapter.getItemCount() - 1;
                keyboard.showSoftInput(viewReference.findViewById(R.id.sendBtn), InputMethodManager.SHOW_FORCED);
            }
            return false;
        });
        //Executes recycler view scroll if required.
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom && isScrollToLastRequired) {
                recyclerView.postDelayed(() -> recyclerView.scrollToPosition(
                        recyclerView.getAdapter().getItemCount() - 1), 100);
            }
        });
        */
    }

    @Override
    public int getTheme() {
        return R.style.NoBackgroundDialogTheme;

    }

    @Override
    public void onResume() {
        super.onResume();
       setUpKeyboard();
    }
}