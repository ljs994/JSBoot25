async function uploadToServer(formObj){

    console.log("서버로업로드처리용 함수....")
    console.log("upload.js uploadToserver.....")
    console.log(formObj)

    const response = await axios({
        method : 'post',
        url : '/upload',
        data : formObj,
        headers : {
            'Content-Type':'multipart/form-data',
        },
    }); // response 비동처리 완료
    return response.data
}// uploadToserver 함수 종료

async function removeFileToServer(uuid, fileName){
    const response = await axios.delete(`/remove/${uuid}_${fileName}`)
    return response.data
}