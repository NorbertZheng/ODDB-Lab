import java.io.File;
import java.util.ArrayList;

public class dataStorer {

	public final static int PAGESIZE = virtualDisk.PAGESIZE;
	public final static int DEFAULT_BIPOINTER = virtualDisk.DEFAULT_BIPOINTER;

	private virtualDisk vdisk;

	public dataStorer(String baseLocation) {
		this.vdisk = new virtualDisk(baseLocation);
	}

	@Override
	protected void finalize() throws Throwable {
		if (this.vdisk != null) {
			this.vdisk.flushToDisk();
		}
		super.finalize();
	}

	/*
	 * encode ArrayList<String> to String, then write system table
	 * @Args:
	 *  data(ArrayList<String>)	: source data
	 * @Ret:
	 *  result(String)			: encode string
	 */
	public static String encode(ArrayList<String> data) {
		return virtualDisk.encode(data);
	}

	/*
	 * decode String to ArrayList<String>, after read system table
	 * @Args:
	 *  code(String)				: source data
	 * @Ret:
	 *  result(ArrayList<String>)	: decode string list
	 */
	public static ArrayList<String> decode(String code) {
		return virtualDisk.decode(code);
	}

	/*
	 * string can parse to int
	 * @Args:
	 *  src(String)		: source string
	 * @Ret:
	 *  flag(boolean)	: whether can string parse to int
	 */
	public static boolean canParseInt(String src) {
		return virtualDisk.canParseInt(src);
	}

	/*
	 * whether exist class
	 * @Args:
	 *  className(String)	: class name
	 * @Ret:
	 *  flag(boolean)		: whether exist class
	 */
	public boolean existClass(String className) {
		return this.vdisk.existClass(className);
	}

	/*
	 * get classStruct
	 * @Args:
	 *  className(String)			: class name
	 * @Ret:
	 *  classStruct(ClassStruct)	: corresponding class struct
	 */
	public classStruct getClassStruct(String className) {
		return this.vdisk.getClassStruct(className);
	}

	/*
	 * get attr list of class
	 * @Args:
	 *  src(classStruct)			: src class struct
	 * @Ret:
	 *  data(ArrayList<String>)		: corresponding attr list
	 */
	public ArrayList<Attribute> getRealAttributeList(classStruct src) {
		return this.vdisk.getRealAttributeList(src);
	}

	/*
	 * set classStruct
	 * @Args:
	 *  className(String)			: class name
	 *  classStruct(classStruct)	: corresponding class struct
	 * @Ret:
	 *  flag(boolean)				: whether set classStruct successfully
	 */
	public boolean setClassStruct(String className, classStruct classStruct) {
		return this.vdisk.setClassStruct(className, classStruct);
	}

	/*
	 * create class
	 * @Args:
	 *  className(String)			: class name
	 *  classStruct(classStruct)	: corresponding class struct
	 * @Ret:
	 *  flag(boolean)				: whether create class successfully
	 */
	public boolean createClass(String className, classStruct classStruct) {
		return this.vdisk.createClass(className, classStruct);
	}

	/*
	 * drop class
	 * @Args:
	 *  className(String)	: class name
	 * @Ret:
	 *  flag(boolean)		: whether drop class successfully
	 */
	public boolean dropClass(String className) {
		return this.vdisk.dropClass(className);
	}

	/*
	 * initial vdisk.currClassName
	 * @Args:
	 *  className(String)		: class name
	 * @Ret:
	 *  None
	 */
	public void initial(String className) {
		this.vdisk.initial(className);
	}

	/*
	 * initial vdisk.currClassName
	 * @Args:
	 *  className(String)		: class name
	 *  _fakeBlockNum(int)		: fake block num
	 *  _fakeBlockOffset(int)	: fake block offset
	 * @Ret:
	 *  None
	 */
	public void initial(String className, int _fakeBlockNum, int _fakeBlockOffset) {
		this.vdisk.initial(className, _fakeBlockNum, _fakeBlockOffset);
	}

	/*
	 * flush buffer data to disk
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)		: whether flush successfully
	 */
	public boolean flushToDisk() {
		return this.vdisk.flushToDisk();
	}

	/*
	 * get next tuple
	 * @Args:
	 *  None
	 * @Ret:
	 *  result(ArrayList<String>)	: next data tuple
	 */
	public ArrayList<String> Next() {
		return this.vdisk.Next();
	}

	/*
	 * get vdisk total offset(index)
	 * @Args:
	 *  None
	 * @Ret:
	 *  offset(int)			: total offset
	 */
	public int getOffset() {
		return this.vdisk.getOffset();
	}

	/*
	 * insert tuple to class
	 * @Args:
	 *  className(String)			: class name
	 *  tuple(ArrayList<String>)	: data tuple
	 * @Ret:
	 *  flag(boolean)				: whether insert tuple successfully
	 */
	public boolean insert(String className, ArrayList<String> tuple) {
		return this.vdisk.insert(className, tuple);
	}

	/*
	 * update tuple in class data
	 * @Args:
	 *  tuple(ArrayList<String>)	: update data tuple
	 * @Ret:
	 *  flag(boolean)				: whether update tuple successfully
	 */
	public boolean update(ArrayList<String> tuple) {
		return this.vdisk.update(tuple);
	}

	/*
	 * delete tuple in class data
	 * @Args:
	 *  None
	 * @Ret:
	 *  flag(boolean)		: whether delete successfully
	 */
	public boolean delete() {
		return this.vdisk.delete();
	}

}

