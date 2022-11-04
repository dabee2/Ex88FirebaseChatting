package com.dabee.ex88firebasechatting;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dabee.ex88firebasechatting.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    Uri imgUri;

    boolean isFirst = true;  //처음 앱을 실행하여 프로필데이터가 없는가?
    boolean isChanged=false; //프로필사진을 변경했는지 여부
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.civ.setOnClickListener(view -> clickImage());
        binding.btn.setOnClickListener(view -> clickBtn());

        //디바이스에 저장되어 있는 로그인정보 (account) 가 있는지 확인
        loadData();

        if(G.nickName != null){
            binding.et.setText(G.nickName);
            Glide.with(this).load(G.profileUrl).into(binding.civ);

            //처음이 아니다. 즉, 이미 접속한 적이 있다.
            isFirst = false;
        }
    }

    //SharedPreference에 저장되어 있는 닉네임과 프로필url 읽어오는 기능
    void loadData(){
        SharedPreferences pref=getSharedPreferences("account",MODE_PRIVATE);
        G.nickName = pref.getString("nickName",null);
        G.profileUrl = pref.getString("profileUrl",null);
    }

    void clickBtn(){
        // 처음이거나 사진을 변경한 적이 있는가?
        if(isFirst || isChanged){
            //닉네임과 프로필이미지를 서버에 DB저장
            saveData();
        }else{
            //저장 없이 곧바로 채팅화면으로 이동
            Intent intent=new Intent(this,ChattingActivity.class);
            startActivity(intent);

            finish();
        }
    }
    void saveData(){
        //이미지 선택을 안하면 채팅 못하도록
        if(imgUri==null) return;

        //EditText로 부터 닉네임 가져오기
        G.nickName= binding.et.getText().toString();

        //우선 이미지 파일 Firebase Storage(저장소)에 업로드부터 해야함.
        //서버에 저장될 파일명이 중복되지 않도록 날짜를 이용하기
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String fileName = sdf.format(new Date())+".png";

        //Firebase Cloud Storage에 파일 업로드
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference imgRef= firebaseStorage.getReference("profile/IMG_"+fileName);

        //이미지파일 업로드
        imgRef.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //업로드에 성공하였으니
                //업로드된 파일의 [다운로드 URL]을 얻어오기(서버에 있는 이미지의 인터넷경로 URL)
                imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) { //파라미터 uri : 다운로드 URL
                        // 다운로드 URL을 문자열로 변환하여 얻어오기
                        G.profileUrl= uri.toString();
                        Toast.makeText(MainActivity.this, "프로필 이미지 저장 완료 \n"+ G.profileUrl, Toast.LENGTH_SHORT).show();

                        //1. 서버 Firebase Firestore Database 에 닉네임과 프로필 url을 저장
                        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                        // 'profiles' 라는 이름의 Collection 참조객체 (없으면 생성, 있으면 참조)
                        CollectionReference profileRef= firebaseFirestore.collection("profiles");

                        //닉네임을 Document 명으로 하고 필드 '값'으로 이미지 경로 url 을 저장
                        Map<String,String> profile = new HashMap<>();
                        profile.put("profileUrl",G.profileUrl);
                        profileRef.document(G.nickName).set(profile);

                        // 앱을 처음 실행할때 한번만 닉네임과 사진을 입력하도록 phone 에 닉네임과 프로필 url을 저장
                        //2. SharedPreferences 에 저장
                        SharedPreferences pref = getSharedPreferences("account",MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();

                        editor.putString("nickName",G.nickName);
                        editor.putString("profileUrl",G.profileUrl);

                        editor.commit();

                        // 저장완료했으니.. 채팅화면으로 이동
                        Intent intent= new Intent(MainActivity.this,ChattingActivity.class);
                        startActivity(intent);

                        finish();

                    }
                });
            }
        });

    }

    void clickImage(){
        // 사진 앱을 실행
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        resultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_CANCELED) return;

            imgUri= result.getData().getData();
            Glide.with(MainActivity.this).load(imgUri).into(binding.civ);

            // 사진을 변경했다.. 라는 표식
            isChanged = true;
        }
    });
}