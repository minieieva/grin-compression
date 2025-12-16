package edu.grinnell.csc207.compression;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

/**
 * A HuffmanTree derives a space-efficient coding of a collection of byte
 * values.
 *
 * The huffman tree encodes values in the range 0--255 which would normally
 * take 8 bits.  However, we also need to encode a special EOF character to
 * denote the end of a .grin file.  Thus, we need 9 bits to store each
 * byte value.  This is fine for file writing (modulo the need to write in
 * byte chunks to the file), but Java does not have a 9-bit data type.
 * Instead, we use the next larger primitive integral type, short, to store
 * our byte values.
 */
public class HuffmanTree {
    private Node root;

    private static class Node{
        int byteValue;
        Node left;
        Node right;
        int size;

        public Node(int byteValue, Node left, Node right){
            this.size = -1;
            //right.size + left.size;
            this.right = right;
            this.left = left;
            this.byteValue = byteValue;
        }

        public Node(int byteValue, int size, Node right, Node left){
            this.size = size;
            //right.size + left.size;
            this.right = right;
            this.left = left;
            this.byteValue = byteValue;
        }
    }

    /**
     * Constructs a new HuffmanTree from a frequency map.
     * @param freqs a map from 9-bit values to frequencies.
     */
    public HuffmanTree (Map<Short, Integer> freqs) {
        freqs.put((short) 256, 1);
        
        //Construct priority queue
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(leafOrInternalNode -> leafOrInternalNode.size));
        for(Entry<Short, Integer> entry : freqs.entrySet()){
            Node leaf = new Node(entry.getKey(), entry.getValue(), null, null);
            queue.add(leaf);
        }

        //build a Huffman tree
        while(queue.size()>=2){
            Node top = queue.poll();
            Node second = queue.poll();
            int sizeCompined = top.size + second.size;
            Node internal = new Node(-1, sizeCompined, top, second);
            queue.add(internal);
        }
        this.root = queue.poll();
    }

    /**
     * Deserialized the input and adds values into Huffman tree
     * @param in the input file (as a BitInputStream)
     * @return the root node for Huffman tree
     */
    private Node constructHuffmanTreeHelper(BitInputStream in) {
        if (in.readBit() == 0) {
            int byteValue = in.readBits(9);
            return new Node(byteValue, null, null);
        }
        else {
            Node left = constructHuffmanTreeHelper(in);
            Node right = constructHuffmanTreeHelper(in);
            return new Node(-1, left, right);
        }
    }
    /**
     * Constructs a new HuffmanTree from the given file.
     * @param in the input file (as a BitInputStream)
     */
    public HuffmanTree (BitInputStream in) {
        // TODO: fill me in!
        //Original file is in a serialized format
        //Deserialize
        this.root = constructHuffmanTreeHelper(in);
    }

    /**
     * Writes this HuffmanTree to the given file as a stream of bits in a
     * serialized format.
     * @param current the current node in a tree
     * @param out the output file as a BitOutputStream
     */
    public void serializeHelper(Node current, BitOutputStream out){
        if(current.left == null && current.right == null){
                out.writeBit(0);
                out.writeBits(current.byteValue, 9);
            }
            else {
                out.writeBit(1);
                serializeHelper(current.left, out);
                serializeHelper(current.right, out);
            }
    }

    /**
     * Writes this HuffmanTree to the given file as a stream of bits in a
     * serialized format.
     * @param out the output file as a BitOutputStream
     */
    public void serialize (BitOutputStream out) {
        // TODO: fill me in!
            serializeHelper(this.root, out);
    }
   

    /**
     * Generates codes for each byte value in the Huffman tree
     * @param current the current node in a tree
     * @param codes a map from byte values to their codes
     * @param code the current code
     */
    public void getCodes(Node current, Map<Integer, String> codes, String code){
        if(current.left == null && current.right == null){
            codes.put(current.byteValue, code);
            return;
        }
        getCodes(current.left, codes, code + "0");
        getCodes(current.right, codes, code + "1");
    }


    /**
     * Writes the payload (the encoded bits) to the output stream.
     * @param in the input file
     * @param out the output file
     */
    public void writePayload(BitInputStream in, BitOutputStream out){
        Map<Integer, String> codes = new HashMap<>();
        getCodes(this.root, codes, "");

        while(true){
            int character = in.readBits(8);
            if (character == -1){
                break;
            }
            String getBits = codes.get(character);
            for(char ch : getBits.toCharArray()){
                if (ch == '1') {
                    out.writeBit(1);
                } else {
                    out.writeBit(0);
                }
            }
        }
        String eofBits = codes.get(256);
        for(char ch : eofBits.toCharArray()){
            if (ch == '1') {
                out.writeBit(1);
            } else {
                out.writeBit(0);
            }
        }
    }

    /**
     * Encodes the file given as a stream of bits into a compressed format
     * using this Huffman tree. The encoded values are written, bit-by-bit
     * to the given BitOuputStream.
     * @param in the file to compress.
     * @param out the file to write the compressed output to.
     */
    public void encode (BitInputStream in, BitOutputStream out) {
        out.writeBits(1846, 32);
        serialize(out);
        writePayload(in, out);
    }

    /**
     * Decodes a stream of huffman codes from a file given as a stream of
     * bits into their uncompressed form, saving the results to the given
     * output stream. Note that the EOF character is not written to out
     * because it is not a valid 8-bit chunk (it is 9 bits).
     * @param in the file to decompress.
     * @param out the file to write the decompressed output to.
     */
    public void decode (BitInputStream in, BitOutputStream out) {
        // TODO: fill me in!
        // Constructs a HuffmanTree from the serialized version of the tree
        Node current = this.root;
        while(true){
            int bit = in.readBit();
            if(bit == 0){
                current = current.left;
            }
            else if(bit == 1){
                current = current.right;
            }
            if(current.left == null && current.right == null){
                int value = current.byteValue;
                if (value == 256) {
                    break;
                }
                out.writeBits(current.byteValue, 8);
                current = this.root;
            }
        }
    }
}
