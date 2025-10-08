from typing import List
from client_service import ClientService


class CommandProcessor:
    SPACE = " "
    BGN_TUPLE = "<"
    END_TUPLE = ">"
    PUT = "put"
    READ = "read"
    TAKE = "take"
    EXIT = "exit"
    GET_TUPLE_SPACES_STATE = "getTupleSpacesState"

    def __init__(self, client_service: ClientService) -> None:
        self.client_service = client_service

    def parse_input(self) -> None:
        exit_flag = False
        while not exit_flag:
            try:
                line = input("> ").strip()
                split = line.split(self.SPACE)
                command = split[0]

                if command == self.PUT:
                    self.put(split)
                elif command == self.READ:
                    self.read(split)
                elif command == self.TAKE:
                    self.take(split)
                elif command == self.GET_TUPLE_SPACES_STATE:
                    self.get_tuple_spaces_state()
                elif command == self.EXIT:
                    exit_flag = True
                else:
                    self.print_usage()
            except EOFError:
                break

    def put(self, split: List[str]) -> None:
        # check if the input is valid
        if not self.input_is_valid(split):
            self.print_usage()
            return

        # get the tuple
        tuple_data = split[1]
        self.client_service.put(tuple_data)

    def read(self, split: List[str]) -> None:
        # check if the input is valid
        if not self.input_is_valid(split):
            self.print_usage()
            return

        # get the tuple
        tuple_data = split[1]
        self.client_service.read(tuple_data)

    def take(self, split: List[str]) -> None:
        # check if the input is valid
        if not self.input_is_valid(split):
            self.print_usage()
            return

        # get the tuple
        tuple_data = split[1]
        self.client_service.take(tuple_data)

    def get_tuple_spaces_state(self) -> None:
        self.client_service.getTupleSpacesState()

    def print_usage(self) -> None:
        print(
            "Usage:\n"
            "- put <element[,more_elements]>\n"
            "- read <element[,more_elements]>\n"
            "- take <element[,more_elements]>\n"
            "- getTupleSpacesState\n"
            "- sleep <integer>\n"
            "- exit\n"
        )

    def input_is_valid(self, input_data: List[str]) -> bool:
        if (
            len(input_data) < 2
            or not input_data[1].startswith(self.BGN_TUPLE)
            or not input_data[1].endswith(self.END_TUPLE)
            or len(input_data) > 2
        ):
            return False
        return True
