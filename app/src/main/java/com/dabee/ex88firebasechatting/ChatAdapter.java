package com.dabee.ex88firebasechatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

    Context context;
    ArrayList<MessageItem> messageItems;

    public ChatAdapter() {
    }

    public ChatAdapter(Context context, ArrayList<MessageItem> messageItems) {
        this.context = context;
        this.messageItems = messageItems;
    }

    final int TYPE_MY=0;
    final int TYPE_OTHER=1;

    //리사이클러뷰의 항목뷰가 경우에 따라 다른 모양으로 보여야 할때 사용하는 콜백메소드
    //이 메소드에서 해당 position 에 따른 식별값(View Type으로 부름) 을 정해서 리턴...하면 onCreateViewHolder()의 두번째 파라미터에 viewType으로 전달되어 해당하는 뷰모양을 다르게 만들수 있음.

    @Override
    public int getItemViewType(int position) {
        if (messageItems.get(position).nickName.equals(G.nickName)) return TYPE_MY;
        else return TYPE_OTHER;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        //항목(Item)에 따라 만들 항목뷰(ItemView)의 모양이 다르게 만들기
        //이 메소드의 2번째 파라미터 viewType 이용
        View itemView = null;
        if(viewType==TYPE_MY) itemView=inflater.inflate(R.layout.my_messagebox,parent,false);
        else itemView=inflater.inflate(R.layout.other_messagebox,parent,false);

        return new VH(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MessageItem item = messageItems.get(position);

        holder.tvName.setText(item.nickName);
        holder.tvMsg.setText(item.message);
        holder.tvTime.setText(item.time);

        Glide.with(context).load(item.profileUrl).into(holder.civ);
    }

    @Override
    public int getItemCount() {
        return messageItems.size();
    }

    ///////////////////
    class VH extends RecyclerView.ViewHolder{

        CircleImageView civ;
        TextView tvName;
        TextView tvMsg;
        TextView tvTime;


        public VH(@NonNull View itemView) {
            super(itemView);

            civ= itemView.findViewById(R.id.civ);
            tvName=itemView.findViewById(R.id.tv_name);
            tvMsg=itemView.findViewById(R.id.tv_msg);
            tvTime=itemView.findViewById(R.id.tv_time);
        }
    }///////////////
}
