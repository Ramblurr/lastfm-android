package fm.last.android.widget;

import java.text.NumberFormat;

import fm.last.android.R;
import fm.last.android.RemoteImageHandler;
import fm.last.android.RemoteImageView;
import fm.last.android.Worker;
import fm.last.api.User;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileBubble extends LinearLayout {

    User mUser;
    TextView mFirst;
    TextView mSecond;
    TextView mThird;
    RemoteImageView mAvatar;
    private Worker mProfileImageWorker;
    private RemoteImageHandler mProfileImageHandler;

    public ProfileBubble(Context context) {
        super(context);
        init();
    }

    public ProfileBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init()
    {
        LayoutInflater.from(getContext())
        .inflate(R.layout.profile_bubble, this);
        this.setBackgroundResource(R.drawable.profile_bubble_bg);
        mSecond = (TextView) findViewById(R.id.profile_geo);
        mSecond.setText("Loading Profile...");
    }

    public void setUser(User user) {
        mUser = user;
        mFirst = (TextView) findViewById(R.id.profile_username);
        mThird = (TextView) findViewById(R.id.profile_plays);
        mAvatar = (RemoteImageView) findViewById(R.id.profile_avatar);

        if(user.getRealName() == null)
            mFirst.setText(user.getName());
        else 
            mFirst.setText(user.getRealName());
        String geo = user.getAge() + user.getGender() + ", "
                + user.getCountry();
        mSecond.setText(geo);

        int playcount = Integer.parseInt(mUser.getPlaycount());
        NumberFormat format = NumberFormat.getNumberInstance();
        String count = format.format( playcount );
        String plays = count + " plays";
        if(mUser.getJoinDate() != null)
            plays += " since " + mUser.getJoinDate(); 
        mThird.setText(plays);

        mProfileImageWorker = new Worker("profile image worker");
        mProfileImageHandler = new RemoteImageHandler(mProfileImageWorker
                .getLooper(), mHandler);
        mProfileImageHandler.removeMessages( RemoteImageHandler.GET_REMOTE_IMAGE );
        if( mUser.getImages().length > 0 )
            mProfileImageHandler.obtainMessage( RemoteImageHandler.GET_REMOTE_IMAGE,
                    mUser.getImages()[0].getUrl()).sendToTarget();
    }

    private final Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
            case RemoteImageHandler.REMOTE_IMAGE_DECODED:
                if (mAvatar != null) {
                    mAvatar.setArtwork((Bitmap) msg.obj);
                    mAvatar.invalidate();
                }
                break;
            default:
                break;
            }
        }
    };
}
