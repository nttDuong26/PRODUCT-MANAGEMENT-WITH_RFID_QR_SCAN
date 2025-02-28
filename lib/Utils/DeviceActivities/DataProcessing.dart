
import '../../Assign_Packing_Information/model_information_package.dart';
import '../../Distribution_Module/model.dart';

class DataProcessing{
  static void ProcessData(List<TagEpc> inputData, List<TagEpc> outputData){
    List<TagEpc> uniqueData = inputData.where((newTag) =>
    !outputData.any((existingTag) => existingTag.epc == newTag.epc)).toList(); // Find all tags that are not in the output list
    outputData.addAll(uniqueData); // Add all unique tags to the output list
  }
  static void ProcessDataLBD(List<TagEpcLBD> inputData, List<TagEpcLBD> outputData){
    List<TagEpcLBD> uniqueData = inputData.where((newTag) =>
    !outputData.any((existingTag) => existingTag.epc == newTag.epc)).toList(); // Find all tags that are not in the output list
    uniqueData.forEach((tag) {
      tag.scanDate = DateTime.now();  // Gán thời gian quét cho thẻ
    });
    outputData.addAll(uniqueData); // Add all unique tags to the output list
  }

  static List<TagEpc> ConvertToTagEpcList(List<Map<String, String>> data) {
    return data.map((tag) {
      return TagEpc(
        epc: tag['tagEpc'] ?? '', // Nếu `tag['tagEpc']` null, sử dụng chuỗi rỗng
        count: tag['tagCount'],
        user: tag['tagUser'],
        rssi: tag['tagRssi'],
        tid: tag['tagTid'],
      );
    }).toList();
  }
  static List<TagEpcLBD> ConvertToTagEpcLBDList(List<Map<String, String>> data) {
    return data.map((tag) {
      return TagEpcLBD(
        epc: tag['tagEpc'] ?? '', // Nếu `tag['tagEpc']` null, sử dụng chuỗi rỗng
        count: tag['tagCount'],
        user: tag['tagUser'],
        rssi: tag['tagRssi'],
        tid: tag['tagTid'],
      );
    }).toList();
  }
}