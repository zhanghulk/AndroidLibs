package com.example.diffpatch;

import java.io.IOException;

import com.difflib.DiffDataUtils;

public class ByteTest {
    
    public static void main(String[] args) {
        String listText = "0001111110001011000010000000000011000100110010000001100101010111000000000000001110010011111011111110011001100000001110";
        char[] list = listText.toCharArray();
        byte[] bytes = new byte[200];
        for (int i = 0; i < list.length; i++) {
            //bytes[i] = list[i];
        }
        try {
            System.out.println(DiffDataUtils.decodeData(bytes, true));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
