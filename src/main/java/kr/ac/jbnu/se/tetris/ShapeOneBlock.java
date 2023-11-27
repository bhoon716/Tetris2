package kr.ac.jbnu.se.tetris;

import java.awt.Color;

public class ShapeOneBlock extends Shape{
    public ShapeOneBlock(){
        super();
        coords = new int[][]{ { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 } };
        pieceShape = Tetrominoes.OneBlockShape;
        color = new Color(128, 128, 128);
    }    
}