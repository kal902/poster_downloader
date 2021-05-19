package posterdownloader;

import java.awt.FlowLayout;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import omdb_sdk.OMDB_Client;
import omdb_sdk.Scan_for_movies;
import omdb_sdk.SearchResult;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import omdb_sdk.OMDBNotificationListener;
import org.json.JSONObject;
public class PosterDownloader {
    private JFrame frame;
    private JButton btn_choose_search,btn_download_dir,start;
    private JComboBox combo_type,combo_count;
    private JTextField src_path_tf,dst_path_tf;
    public JTextArea dashboard;
    private String[] types={"movie","series"};
    private String download_count[]={"1","2","3","4","5"};
    
    private int type=0;
    private int dwn_count=0;
    
    public PosterDownloader(){
        frame = new JFrame("PosterDownloader");
        frame.setSize(186, 309);
        FlowLayout fl = new FlowLayout();
        fl.setHgap(7);
        frame.setLayout(fl);
        
        src_path_tf = new JTextField();
        src_path_tf.setText("movies directory");
        src_path_tf.setPreferredSize(new Dimension(114, 20));
        dst_path_tf = new JTextField();
        dst_path_tf.setText("downloads directory");
        dst_path_tf.setPreferredSize(new Dimension(114, 20));
        btn_choose_search = new JButton("+");
        btn_choose_search.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                FileDialog fd = new FileDialog(frame);
                fd.setName("where to scan for movies");
                fd.setMode(FileDialog.LOAD);
                fd.setVisible(true);
                String path = fd.getDirectory();
                if(path!=null){
                    src_path_tf.setText(path);
                }
            }
        });
        
        
        btn_download_dir = new JButton("+");
        btn_download_dir.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                FileDialog fd = new FileDialog(frame);
                fd.setName("where to save images");
                fd.setMode(FileDialog.LOAD);
                fd.setVisible(true);
                String path = fd.getDirectory();
                if(path!=null){
                    dst_path_tf.setText(path);
                }
            }
        });
        
        combo_type = new JComboBox(types);
        combo_type.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent ie) {
                type = combo_type.getSelectedIndex()+1;
            }
        
        });
        
        combo_count = new JComboBox(download_count);
        combo_count.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent ie) {
                dwn_count=combo_count.getSelectedIndex()+1;
            }
        
        });
        
        start = new JButton("start");
        start.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                if(src_path_tf.getText()!="movies directory"){
                    if(dst_path_tf.getText()!="downloads directory"){
                        Helper h = new Helper(src_path_tf.getText(),dst_path_tf.getText(),dwn_count,type);
                        h.start();
                    }else{
                        Helper h = new Helper(src_path_tf.getText(),"",dwn_count,type);
                        h.start();
                    }
                }
            }
        });
        
        
        frame.add(src_path_tf);
        frame.add(btn_choose_search);
        frame.add(dst_path_tf);
        frame.add(btn_download_dir);
        frame.add(new JLabel("search type: "));
        frame.add(combo_type);
        frame.add(new JLabel("poster per movie: "));
        frame.add(combo_count);
        frame.add(start);
        //frame.add(upper_pane);
        dashboard = new JTextArea();
        dashboard.setEditable(true);
        dashboard.setPreferredSize(new Dimension(165,110));
        //JScrollPane scroll = new JScrollPane();
        //scroll.add(dashboard);
        frame.add(new JScrollPane(dashboard));
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        
        Dimension res = Toolkit.getDefaultToolkit().getScreenSize();
        int frame_center_horizontal = frame.getSize().width / 2;
        int frame_center_vertical = frame.getSize().height / 2;
        int screen_center_horizontal = res.width / 2;
        int screen_center_vertical = res.height / 2;
        int x = screen_center_horizontal - frame_center_horizontal;
        int y = screen_center_vertical - frame_center_vertical;
        frame.setLocation(x, y);
        frame.setVisible(true);
    
        
    }
    
    public static void main(String []a){
        new PosterDownloader();
    }
    
    class Helper extends Thread{
        private String search_path,dwn_dir="";
        private int count,type;
        
        public Helper(String src,String dst,int count, int type){
             search_path=src;
             dwn_dir=dst;
             this.count=count;
             this.type=type;
        }
        
        @Override
        public void run(){
            OMDB_Client client = new OMDB_Client();
            client.addListener(new OMDBNotificationListener(){
                @Override
                public void onNotification(String text){
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run(){
                            dashboard.append(text);
                        }
                    });
                }
            });
            if(type!=0){// movie=1,series=2,0=default(both)
                client.setType(type-1);
            }
            Scan_for_movies scan = new Scan_for_movies();
            File files[] = scan.scandir(search_path);
            SearchResult res = new SearchResult();
            res.addListener(new OMDBNotificationListener(){
                @Override
                public void onNotification(String text){
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run(){
                            dashboard.append(text);
                        }
                    });
                }
            });
            for (int i = 0; i < files.length; i++) {

                String mov_name = scan.remove_unwanted_chars(files[i].getName());
                JSONObject json_res = client.search_movie(mov_name);// json response
                // must set json_result, if it is not initialized with the constructor(Scan_for_movies(json_result))
                res.setJson_result(json_res);
                if (count != 0) {
                    res.setMax_download(count); // 0 means default.
                }
                if (dwn_dir != "") {
                    res.setDownload_dir(dwn_dir);
                } else {
                    res.setDownload_dir(search_path);
                }
                res.download_images();

            }
        }
    }
    
}
