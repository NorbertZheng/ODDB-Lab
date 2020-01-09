public class bufferLine {
	final static int MAX_INTEGER = 0x7fffffff;

	public int n_block;
	public int isDirty;
	public byte[] buffer;

	public bufferLine(int blockSize) {
		this.n_block = bufferLine.MAX_INTEGER;
		this.isDirty = 0;
		this.buffer = new byte[blockSize];
	}

	public bufferLine(int blockSize, int n_block) {
		this.n_block = n_block;
		this.isDirty = 0;
		this.buffer = new byte[blockSize];
	}
}

