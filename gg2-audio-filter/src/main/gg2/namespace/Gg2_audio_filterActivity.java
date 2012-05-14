package main.gg2.namespace;

import java.io.IOException;


import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Gg2_audio_filterActivity extends Activity {
    /** Called when the activity is first created. */

    boolean isRecording = false;
   
    

    //this method is initial loading when app starts
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    
        setButtonHandlers();
        enableButtons(false);

    }
    
    //this function sets up the button in the view
    private void setButtonHandlers() {
        ((Button)findViewById(R.id.btnStart)).setOnClickListener(btnClick);
	((Button)findViewById(R.id.btnStop)).setOnClickListener(btnClick);
	}
	
    //this is template to enable viewing of the buttons
	private void enableButton(int id,boolean isEnable){
	        ((Button)findViewById(id)).setEnabled(isEnable);
	}
	
	private void enableButtons(boolean isRecording) {
	        enableButton(R.id.btnStart,!isRecording);
	        enableButton(R.id.btnStop,isRecording);
	}
	
    private void startRecording(){
    	AudioManager audio_service = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

	   audio_service.setSpeakerphoneOn(false);
	   audio_service.setMode(AudioManager.MODE_IN_CALL);
	   audio_service.setRouting(AudioManager.MODE_NORMAL,
	   AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
	   
	   Record record = new Record();
	      
	   record.start();


    }
 
    private void stopRecording(){
           isRecording = false;
    }
    
    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btnStart:{
                    AppLog.logString("Start Recording");
                    
                    enableButtons(true);
                    startRecording();
                                    
                    break;
                }
                case R.id.btnStop:{
                    AppLog.logString("Stop Recording");
                    
                    enableButtons(false);
                    stopRecording();
                    
                    break;
                }
            }
        }
    }; 
    

    
//this the backgroud thread that reads and write into the buffer    
public class Record extends Thread
{
        static final int bufferSize = 200000;
        final short[] buffer = new short[bufferSize];
        
        public void run() {  
        isRecording = true;
          android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
          int buffersize = AudioRecord.getMinBufferSize(11025,
          AudioFormat.CHANNEL_CONFIGURATION_MONO,
          AudioFormat.ENCODING_PCM_16BIT);

                         AudioRecord arec = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                         11025,
                                         AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                         AudioFormat.ENCODING_PCM_16BIT,
                                         buffersize);

                         AudioTrack atrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                                         11025,
                                         AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                         AudioFormat.ENCODING_PCM_16BIT,
                                         buffersize,
                                         AudioTrack.MODE_STREAM);

                         setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);


                         atrack.setPlaybackRate(11025);

                         byte[] buffer = new byte[buffersize];
                         arec.startRecording();
                         atrack.play();

                         while(isRecording) {
                                
                        	 arec.read(buffer, 0, buffersize);
                                
                        	 try{
                                 DoubleFFT_1D fft = new DoubleFFT_1D(buffer.length);
                                 if (false){
                                	 throw new IOException("gili!");
                                 }
                                 Log.d("the size of the buffer is ", " " + buffer.length);
                                 double[] audioDataDoubles = new double[buffer.length*2];
                                 
                                 for (int j=0 ; j < buffer.length ; j++){
                                 	audioDataDoubles[j] = (double)buffer[j];
                                 }
                         
                                 
                                 fft.realForward(audioDataDoubles);
                                 fft.realInverse(audioDataDoubles, true);
                                 
                                 byte[] invFFTBuffer = new byte[buffer.length];//this is half of the size
                                 
                                 for (int j=0 ; j < invFFTBuffer.length ; j++){
                                 	invFFTBuffer[j] = (byte)audioDataDoubles[j];
                                 }
                                 
                                 //atrack.write(buffer, 0, buffer.length);
                             
                                 atrack.write( invFFTBuffer, 0, invFFTBuffer.length);
                                 }
                                 catch(IOException e){
                                	 e.printStackTrace();
                                 }
                         }

                         arec.stop();
                         atrack.stop();
            }
    }

}