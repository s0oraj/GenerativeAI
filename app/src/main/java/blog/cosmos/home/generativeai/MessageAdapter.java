package blog.cosmos.home.generativeai;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**This class populates our Recyclerview with its dataset which contains user messages and bot messages**/
public class MessageAdapter extends RecyclerView.Adapter {


    private List<Message> mMessageModalArrayList;
    private Context context;
    OnPressed onPressed;
    private FragmentManager fragmentManager;

    

    public MessageAdapter(ArrayList<Message> mMessageModalArrayList, Context context, FragmentManager fragmentManager) {
        this.mMessageModalArrayList = mMessageModalArrayList;
        Collections.reverse(mMessageModalArrayList);
        this.context = context;
        this.fragmentManager=fragmentManager;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
    
        switch (viewType) {
            case 0:

                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_message, parent, false);

                return new UserViewHolder(view);
            case 1:
       
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_message, parent, false);
                return new BotViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // this method is use to set data to our layout file.
        Message modal = mMessageModalArrayList.get(position);
        switch (modal.getSender()) {
            case "user":

                ((UserViewHolder) holder).userTV.setText(modal.getMessage());
                break;
            case "bot": {

                String imageUrl = mMessageModalArrayList.get(position).getImageUrl();
                if( modal.getMessage()!=null){
                    if(!modal.getMessage().isEmpty()){
                        ((BotViewHolder) holder).botTV.setVisibility(View.VISIBLE);
                        ((BotViewHolder) holder).botTV.setText(modal.getMessage());

                    }
                } else{
                    ((BotViewHolder) holder).botTV.setVisibility(View.GONE);
                }
                if(imageUrl!=null){
                    Glide.with(context.getApplicationContext())
                            .load(mMessageModalArrayList.get(position).getImageUrl())
                            .override(600,200)
                            .fitCenter()
                            .into(((BotViewHolder) holder).imageView);
                    ((BotViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            /*String imageUrl = mMessageModalArrayList.get(position).getImageUrl();
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(imageUrl));
                            context.startActivity(i);*/


                            Bundle bundle = new Bundle();
                            bundle.putString("url", imageUrl);

                            CustomDialogFragment dialogFragment=new CustomDialogFragment();
                            dialogFragment.setArguments(bundle);
                            dialogFragment.show(fragmentManager,"My  Fragment");


                        }
                    });


                }

               /*
              Glide.with(context.getApplicationContext())
                        .load(mMessageModalArrayList.get(position).getImageUrl())
                        .placeholder(new ColorDrawable(context.getResources().getColor(R.color.bg_color))
                        .into(holder.imageView);

                */

                    }

                break;
        }
    }

    @Override
    public int getItemCount() {
        
        return mMessageModalArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // below line of code is to set position.
        switch (mMessageModalArrayList.get(position).getSender()) {
            case "user":
                return 0;
            case "bot":
                return 1;
            default:
                return -1;
        }
    }

    public void addAll(List<Message> data){
        if (data != null && !data.isEmpty()) {
          
            mMessageModalArrayList = data;
       
            notifyDataSetChanged();
        }
    }


    public void clear(){
        if(mMessageModalArrayList!=null && !mMessageModalArrayList.isEmpty()) {
            int size = mMessageModalArrayList.size();
            mMessageModalArrayList.clear();

            notifyItemRangeRemoved(0, size);
        }
    }

    public void onPressedMethod(OnPressed onPressed) {
        this.onPressed = onPressed;
    }

    public interface OnPressed {
        void onImageViewClicked(int position, String imageUrl);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

      
        TextView userTV;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
          
            userTV = itemView.findViewById(R.id.idTVUser);
        }
    }

    public static class BotViewHolder extends RecyclerView.ViewHolder {


        TextView botTV;
        ImageView imageView;

        public BotViewHolder(@NonNull View itemView) {
            super(itemView);
           
            botTV = itemView.findViewById(R.id.idTVBot);
            imageView = itemView.findViewById(R.id.imageView);

        }

    }
}
