
package com.example.diffpatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {

    public static void log_start() {/*
        new Thread(new Runnable() {
            @Override
            public void run() {

                Process p = new Process();

                java.lang.Process process = null;
                try {
                    process = Runtime.getRuntime().exec("logcat -v threadtime | grep " + p.myPid());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                try {
                    while ((line = bufferedReader.readLine()) != null) {

                        if (fr == null)
                        {
                            Date d = new Date();
                            try {

                                if (getPackageName().contains("byod")) {

                                    File file = new File("/o.Rg_sDcaRddIR/voip");

                                    if (!file.exists())
                                        file.mkdirs();

                                    fr = new FileWriter("/mnt/sdcard/" + "pjsip_logcat_"
                                            + DateFormat.format("yy-MM-dd_kkmmss", d), true);
                                } else {

                                    File file = new File("/sdcard/voip");

                                    if (!file.exists())
                                        file.mkdirs();

                                    fr = new FileWriter("/sdcard/voip/" + "pjsip_logcat_"
                                            + DateFormat.format("yy-MM-dd_kkmmss", d), true);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                return;
                            }
                        }

                        try {
                            fr.write(line + "\n");
                            fr.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                            fr.flush();
                            fr.close();
                            fr = null;
                        }

                        long fr_time_now = System.currentTimeMillis();

                        if (fr_time == 0)
                        {
                            fr_time = fr_time_now;
                        }

                        if ((fr_time_now - fr_time) > 7200000)
                        {
                            if (fr != null)
                            {
                                fr.flush();
                                fr.close();
                            }

                            fr = null;
                            fr_time = fr_time_now;

                            StatFs stat = null;
                            String path = null;
                            if (getPackageName().contains("byod")) {
                                path = "/mnt/sdcard";
                            }
                            else {
                                path = "/sdcard";
                            }

                            {
                                try {
                                    File f = new File(path + "/voip/");

                                    String[] paths = f.list();

                                    Arrays.sort(paths);

                                    if (paths.length > 12)
                                    {
                                        File f1 = new File("/sdcard/voip/" + paths[0]);
                                        f1.delete();
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                        }

                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    try {
                        fr.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

            }
        }).start();

    */}
}
