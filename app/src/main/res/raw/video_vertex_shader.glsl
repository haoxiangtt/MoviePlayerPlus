uniform mat4 uMVPMatrix;
uniform mat4 uSTMatrix;
attribute vec4 vPosition;
attribute vec4 inputTextureCoordinate;
varying vec2 textureCoordinate;

void main() {
    textureCoordinate = (uSTMatrix * inputTextureCoordinate).xy;
    gl_Position = uMVPMatrix * vPosition;
}
