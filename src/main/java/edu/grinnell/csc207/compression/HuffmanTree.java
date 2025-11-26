package edu.grinnell.csc207.compression;

import java.util.Map;

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

        public Node(int byteValue, Node right, Node left){
            this.size = -1;
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
        // TODO: fill me in!
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
            return new Node(-1, right, left);
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
     * @param out the output file as a BitOutputStream
     */
    public static void serialize (BitOutputStream out) {
        // TODO: fill me in!
        //2)
    }
   
    /**
     * Encodes the file given as a stream of bits into a compressed format
     * using this Huffman tree. The encoded values are written, bit-by-bit
     * to the given BitOuputStream.
     * @param in the file to compress.
     * @param out the file to write the compressed output to.
     */
    public void encode (BitInputStream in, BitOutputStream out) {
        // TODO: fill me in!
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
        //At this point I have a huffTree
        Node current = this.root;
        while(true){
            int bit = in.readBit();
            if (bit == -1) return;
            if(bit == 0){
                current = current.left;
            }
            else if(bit == 1){
                current = current.right;
            }
            if(current.left == null && current.right == null){
                int value = current.byteValue;
                if (value == 256) {
                    return;
                }
                out.writeBits(current.byteValue, 8);
                current = this.root;
            }
        }
    }
}
