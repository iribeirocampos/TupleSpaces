import sys
import grpc

sys.path.insert(1, "../Contract/target/generated-sources/protobuf/python")
import TupleSpaces_pb2_grpc as pb2_grpc
import TupleSpaces_pb2 as pb2


class ClientService:

    def __init__(self, host_port: str, client_id: int) -> None:
        self.host_port = host_port
        self.client_id = client_id
        channel = grpc.insecure_channel(host_port)
        self.stub = pb2_grpc.TupleSpacesStub(channel)

    def put(self, tuple_data: str):
        try:
            self.stub.put(pb2.PutRequest(newTuple=tuple_data))
            print("OK\n")
        except Exception as e:
            print("Error: ", e)

    def read(self, tuple_data: str) -> None:
        try:
            response = self.stub.read(pb2.ReadRequest(searchPattern=tuple_data))
            if response != None:
                print("OK")
                print(response.result + "\n")
        except Exception as e:
            print("Error: ", e)

    def take(self, tuple_data: str) -> None:
        try:
            response = self.stub.take(pb2.TakeRequest(searchPattern=tuple_data))
            if response != None:
                print("OK")
                print(response.result + "\n")
            print("")
        except Exception as e:
            print("Error: ", e)

    def getTupleSpacesState(self) -> None:
        try:
            response = self.stub.getTupleSpacesState(pb2.getTupleSpacesStateRequest())
            if response != None:
                print("OK")
                print(response.result + "\n")
            print("")
        except Exception as e:
            print("Error: ", e)
